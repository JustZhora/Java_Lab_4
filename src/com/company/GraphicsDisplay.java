package com.company;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.LinkedList;
import javax.swing.*;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    // Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private boolean showIntegrals = false;
    // Используемый масштаб отображения
    private double scale;
     // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private boolean turnGraph = false;
    private BasicStroke markerStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;
    private Font smallfont;
    public GraphicsDisplay() {
        // Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
        // Сконструировать необходимые объекты, используемые в рисовании
        // Перо для рисования графика
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
        // Перо для рисования осей координат
                BasicStroke.JOIN_ROUND, 10.0f, new float[] {10, 10, 10, 10, 10, 10, 30 , 30, 30,30,30,30}, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        // Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        // Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);
    }
    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
        // Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
        repaint();
    }
     // Методы-модификаторы для изменения параметров отображения графика
     // Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }
    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    public void setShowIntegrals(boolean showIntegrals) {
        this.showIntegrals = showIntegrals;
        repaint();
    }
    // Метод отображения всего компонента, содержащего график
    @Override
    public void paintComponent (Graphics g){
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }
        if (!turnGraph) {
            double scaleX = getSize().getWidth() / (maxX - minX);
            double scaleY = getSize().getHeight() / (maxY - minY);
            scale = Math.min(scaleX, scaleY);
            if (scale == scaleX) {
                double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
            if (scale == scaleY) {
                double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 4;// тут въебать /4 и дописать if
                maxX += xIncrement;
                minX -= xIncrement;
            }
        } else {
            double scaleX = getSize().getHeight() / (maxX - minX);
            double scaleY = getSize().getWidth() / (maxY - minY);
            scale = Math.min(scaleX, scaleY);
            if (scale == scaleY) {
                double xIncrement = (getSize().getHeight() / scale - (maxX - minX)) / 2;
                maxX += xIncrement;
                minX -= xIncrement;
            }
            if (scale == scaleX) {
                double yIncrement = (getSize().getWidth() / scale - (maxY - minY)) / 2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
        }
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        if (turnGraph) {
            rotatePanel(canvas);
        }
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showIntegrals) paintIntegrals(canvas);
        if (showMarkers) paintMarkers(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }
     // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.magenta);
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }
    protected void paintIntegrals(Graphics2D canvas) {
        LinkedList<Integer> indexses = new LinkedList<>();
        Double domens = 0.0;
        GeneralPath path = new GeneralPath();
        for (int i = 0; i < graphicsData.length - 1; i++) {
            System.out.println("X: " + graphicsData[i][0] + " Y: " + graphicsData[i][1] + " i: " + i);
            if ((graphicsData[i][1] < 0 == graphicsData[i + 1][1] >= 0) || (graphicsData[i][1] == 0)) {
                if (domens != 0) {
                    domens += 1;
                    if (graphicsData[i][1] == 0)
                        indexses.add(i - 1);
                    else
                        indexses.add(i);
                    indexses.add(i);
                    System.out.println("End+Start");
                    if(graphicsData[i+1][1]==0) {
                        i++;
                    }
                    System.out.println("X: " + graphicsData[i][0] + " Y: " + graphicsData[i][1] + " i: " + i);
                    continue;
                } else {
                    indexses.add(i);
                    System.out.println("Start");
                    domens += 0.5;
                }
            }
        }
        LinkedList<Double> xcoordinates = new LinkedList<>();
        for (int i = 0; i < 2 * domens.intValue(); i++) {
            xcoordinates.add(-graphicsData[indexses.get(i)][1] / (graphicsData[indexses.get(i) + 1][1] - graphicsData[indexses.get(i)][1]) * (graphicsData[indexses.get(i) + 1][0] - graphicsData[indexses.get(i)][0]) + graphicsData[indexses.get(i)][0]);
            System.out.println("Координата x пересечения c Ox с индексом " + i + " " + xcoordinates.get(i) + " на интервале от " + indexses.get(i) + " до " + (indexses.get(i) + 1));
        }
        int k = 0;
        Double[] integral = new Double[xcoordinates.size() / 2];
        for (int i = 0; i < xcoordinates.size() / 2; i++) {
            integral[i] = 0.0;
        }
        Double maxy = 0.0;
        Double miny = 0.0;
        Double[] averagey = new Double[xcoordinates.size() / 2];
        for (int i = 0; i < graphicsData.length; i++) {
            System.out.println("INDEX: "+ i+ " left " +xcoordinates.get(k)+"<="+graphicsData[i][0]+"<"+xcoordinates.get(k+1)+ " ? ");
            if (graphicsData[i][0] >= xcoordinates.get(k) && graphicsData[i][0] < xcoordinates.get(k + 1)) {
                if (maxy < graphicsData[i][1]) {
                    maxy = graphicsData[i][1];
                }
                if (miny > graphicsData[i][1]) {
                    miny = graphicsData[i][1];
                }
                if (graphicsData[i - 1][0] <= xcoordinates.get(k)) {
                    integral[k / 2] += Math.abs((graphicsData[i][0] - xcoordinates.get(k)) * graphicsData[i][1] / 2);
                    canvas.setColor(Color.red);
                    Point2D.Double point = xyToPoint(xcoordinates.get(k), 0);
                    path.moveTo(point.getX(), point.getY());
                    System.out.println("The line moved to its initial position, x = " + point.getX() + " on the itaration i = " + i);
                    point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                    path.lineTo(point.getX(), point.getY());
                }
                if (graphicsData[i + 1][0] >= xcoordinates.get(k + 1)) {
                    Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                    path.lineTo(point.getX(), point.getY());
                    point = xyToPoint(xcoordinates.get(k + 1), 0);
                    path.lineTo(point.getX(), point.getY());
                    integral[k / 2] += Math.abs(graphicsData[i][1] / 2 * (xcoordinates.get(k + 1) - graphicsData[i][0]));
                    path.closePath();
                    System.out.println("The line was closed , x = " + point.getX() + " on the itaration i = " + i);
                    canvas.fill(path);
                    canvas.draw(path);
                    if (maxy == 0.0)
                        averagey[k / 2] = miny;
                    else
                        averagey[k / 2] = maxy;
                    if (k >= xcoordinates.size() - 2) break;
                    k += 2;
                    maxy = 0.0;
                    miny = 0.0;
                }
                if(!(graphicsData[i + 1][0] >= xcoordinates.get(k + 1))&&!(graphicsData[i - 1][0] <= xcoordinates.get(k))) {
                    integral[k / 2] += Math.abs((graphicsData[i][0] - graphicsData[i - 1][0]) * (graphicsData[i][1] + graphicsData[i - 1][1]) / 2);
                    Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                    path.lineTo(point.getX(), point.getY());
                }

            }
        }
        System.out.println("Integral" + (int) (k / 2) + " = " + integral[k / 2]);

        canvas.setFont(smallfont);
        FontRenderContext context = canvas.getFontRenderContext();
        for (
                int i = 0; i < xcoordinates.size() / 2; i++) {
            canvas.setColor(Color.black);
            Rectangle2D bounds = smallfont.getStringBounds(String.format("%.3f", integral[i]), context);
            System.out.println(bounds.getX());
            canvas.drawString(String.format("%.3f", integral[i]), (float) (xyToPoint(xcoordinates.get(2 * i) + (xcoordinates.get(2 * i + 1) - xcoordinates.get(2 * i)) / 2 - bounds.getX(), averagey[i] / 2).getX()), (float) xyToPoint(xcoordinates.get(2 * i) + (xcoordinates.get(2 * i + 1) - xcoordinates.get(2 * i)) / 2, averagey[i] / 2).getY());
        }
    }
    protected void rotatePanel(Graphics2D canvas){
        canvas.translate(0, getHeight());
        canvas.rotate(-Math.PI/2);
    }
    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        for (Double[] point : graphicsData) {
            int size = 5;
            Ellipse2D.Double marker = new Ellipse2D.Double();
            Point2D.Double center = xyToPoint(point[0], point[1]);
            Point2D.Double corner = shiftPoint(center, size, size);
            marker.setFrameFromCenter(center, corner);
            Line2D.Double line = new Line2D.Double(shiftPoint(center, -size, 0), shiftPoint(center, size, 0));
            Boolean highervalue = true;
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(2);
            DecimalFormatSymbols dottedDouble =
                    formatter.getDecimalFormatSymbols();
            dottedDouble.setDecimalSeparator('.');
            formatter.setDecimalFormatSymbols(dottedDouble);
            String temp = formatter.format(Math.abs(point[1]));
            temp = temp.replace(".", "");
            for (int i = 0; i < temp.length() - 1; i++) {
                if (temp.charAt(i) != 46 && (int) temp.charAt(i) > (int) temp.charAt(i + 1)) {
                    highervalue = false;
                    break;
                }
            }
            if (highervalue) {
                canvas.setColor(Color.BLACK);//!!!!!!
            }
            canvas.draw(line);
            line.setLine(shiftPoint(center, 0, -size), shiftPoint(center, 0, size));
            canvas.draw(line);
            canvas.draw(marker); // Начертить контур маркера
            canvas.setColor(Color.BLUE);//tut menjat!!!!
        }
    }
    // Метод, обеспечивающий отображение осей координат
    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if (minX<=0.0 && maxX>=0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
                    xyToPoint(0, minY)));
            // Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()+5,
                    arrow.getCurrentPoint().getY()+20);
            arrow.lineTo(arrow.getCurrentPoint().getX()-10,
                    arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float)labelPos.getX() + 10,
                    (float)(labelPos.getY() - bounds.getY()));
            Rectangle2D centerBounds = axisFont.getStringBounds("0", context);
            Point2D.Double centerLabelPos = xyToPoint(0, 0);
            canvas.drawString("0", (float)centerLabelPos.getX() + 10,
                    (float)(centerLabelPos.getY() - centerBounds.getY()));
        }
        if (minY<=0.0 && maxY>=0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
            
            // Стрелка оси X
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()-20,
                    arrow.getCurrentPoint().getY()-5);
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY()+10);
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float)(labelPos.getX() -
                    bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
        }
    }
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }
    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX,
                                        double deltaY) {
        // Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
        // Задать еѐ координаты как координаты существующей точки + заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
}
