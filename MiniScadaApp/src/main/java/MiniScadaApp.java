import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MiniScadaApp extends Application {

    private Label tempLabel, levelLabel, pressureLabel;
    private Button pumpButton, valveButton, saveLogButton;
    private TextArea logArea;
    private Circle pumpIndicator, valveIndicator;

    private XYChart.Series<Number, Number> tempSeries;
    private XYChart.Series<Number, Number> pressureSeries;
    private XYChart.Series<Number, Number> levelSeries;
    private int timeCounter = 0;

    private Controller mainController;
    private final Random random = new Random();

    @Override
    public void start(Stage primaryStage) {

        mainController = new Controller();

        tempLabel = new Label();
        levelLabel = new Label();
        pressureLabel = new Label();

        pumpButton = new Button("Запустить насос");
        valveButton = new Button("Открыть клапан");
        saveLogButton = new Button("Сохранить лог");

        pumpButton.setOnAction(e -> togglePump(mainController));
        valveButton.setOnAction(e -> toggleValve(mainController));
        saveLogButton.setOnAction(e -> saveLog());

        pumpIndicator = new Circle(10, Color.RED);
        valveIndicator = new Circle(10, Color.RED);

        HBox controlBox = new HBox(10, pumpButton, pumpIndicator, valveButton, valveIndicator, saveLogButton);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);

        LineChart<Number, Number> tempChart = createLineChart("Температура во времени", "Время (с)", "°C");
        LineChart<Number, Number> pressureChart = createLineChart("Давление во времени", "Время (с)", "бар");
        LineChart<Number, Number> levelChart = createLineChart("Уровень жидкости во времени", "Время (с)", "%");

        tempSeries = new XYChart.Series<>();
        pressureSeries = new XYChart.Series<>();
        levelSeries = new XYChart.Series<>();
        tempChart.getData().add(tempSeries);
        pressureChart.getData().add(pressureSeries);
        levelChart.getData().add(levelSeries);

        VBox root = new VBox(10,
                new HBox(20, tempLabel, levelLabel, pressureLabel),
                controlBox,
                tempChart, pressureChart, levelChart,
                logArea);
        root.setStyle("-fx-padding: 15; -fx-font-size: 14px;");

        Scene scene = new Scene(root, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Мини-SCADA симулятор");
        primaryStage.show();

        startSensorSimulation();
    }

    private LineChart<Number, Number> createLineChart(String title, String xLabel, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        return chart;
    }

    private void togglePump(Controller c) {
        c.pumpOn = !c.pumpOn;
        if (c.pumpOn) {
            pumpButton.setText("Остановить насос");
            pumpIndicator.setFill(Color.GREEN);
            addLog("Насос запущен");
        } else {
            pumpButton.setText("Запустить насос");
            pumpIndicator.setFill(Color.RED);
            addLog("Насос остановлен");
        }
    }

    private void toggleValve(Controller c) {
        c.valveOpen = !c.valveOpen;
        if (c.valveOpen) {
            valveButton.setText("Закрыть клапан");
            valveIndicator.setFill(Color.GREEN);
            addLog("Клапан открыт");
        } else {
            valveButton.setText("Открыть клапан");
            valveIndicator.setFill(Color.RED);
            addLog("Клапан закрыт");
        }
    }

    private void updateSensorLabels(Controller c) {
        tempLabel.setText(String.format("Температура: %.1f°C", c.temperature));
        levelLabel.setText(String.format("Уровень: %.1f%%", c.level));
        pressureLabel.setText(String.format("Давление: %.2f бар", c.pressure));

        if (c.temperature > 80 && c.pumpOn) autoStopPump(c);
        if (c.pressure > 4.0 && !c.valveOpen) autoOpenValve(c);

        if (c.temperature <= 80) c.tempAlarmTriggered = false;
        if (c.level >= 10) c.levelAlarmTriggered = false;
        if (c.pressure <= 4.0) c.pressureAlarmTriggered = false;

        if (c.temperature > 80) playAlarmOnce(c, "temp");
        if (c.level < 10) playAlarmOnce(c, "level");
        if (c.pressure > 4.0) playAlarmOnce(c, "pressure");
    }

    private void autoStopPump(Controller c) {
        c.pumpOn = false;
        pumpButton.setText("Запустить насос");
        pumpIndicator.setFill(Color.RED);
        addLog("✅ Защита: насос автоматически остановлен");
    }

    private void autoOpenValve(Controller c) {
        c.valveOpen = true;
        valveButton.setText("Закрыть клапан");
        valveIndicator.setFill(Color.GREEN);
        addLog("✅ Защита: клапан автоматически открыт");
    }

    private void playAlarmOnce(Controller c, String type) {
        boolean triggered = switch (type) {
            case "temp" -> c.tempAlarmTriggered;
            case "pressure" -> c.pressureAlarmTriggered;
            case "level" -> c.levelAlarmTriggered;
            default -> false;
        };
        if (!triggered) {
            playAlarm();
            switch (type) {
                case "temp" -> c.tempAlarmTriggered = true;
                case "pressure" -> c.pressureAlarmTriggered = true;
                case "level" -> c.levelAlarmTriggered = true;
            }
        }
    }

    private void addLog(String message) {
        String time = LocalTime.now().withNano(0).toString();
        logArea.appendText("[" + time + "] " + message + "\n");
    }

    private void saveLog() {
        try (FileWriter writer = new FileWriter("scada_log.txt", true)) {
            writer.write(logArea.getText());
            addLog("Лог сохранен в scada_log.txt");
        } catch (IOException e) {
            addLog("Ошибка при сохранении лога: " + e.getMessage());
        }
    }

    private void playAlarm() {
        new Thread(() -> {
            try {
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(44100, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                for (int i = 0; i < 44100 / 100; i++) {
                    double angle = i / (44100.0 / 1000) * 2.0 * Math.PI;
                    buf[0] = (byte) (Math.sin(angle) * 100);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void startSensorSimulation() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mainController.updateSimulation(random);

                double finalTemp = mainController.temperature;
                double finalLevel = mainController.level;
                double finalPressure = mainController.pressure;
                int finalTime = timeCounter++;

                Platform.runLater(() -> {
                    updateSensorLabels(mainController);

                    tempSeries.getData().add(new XYChart.Data<>(finalTime, finalTemp));
                    pressureSeries.getData().add(new XYChart.Data<>(finalTime, finalPressure));
                    levelSeries.getData().add(new XYChart.Data<>(finalTime, finalLevel));

                    if (tempSeries.getData().size() > 50) tempSeries.getData().remove(0);
                    if (pressureSeries.getData().size() > 50) pressureSeries.getData().remove(0);
                    if (levelSeries.getData().size() > 50) levelSeries.getData().remove(0);
                });
            }
        }, 0, 1000);
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class Controller {
        double temperature = 20.0;
        double level = 50.0;
        double pressure = 1.0;
        boolean pumpOn = false;
        boolean valveOpen = false;

        boolean tempAlarmTriggered = false;
        boolean pressureAlarmTriggered = false;
        boolean levelAlarmTriggered = false;

        void updateSimulation(Random random) {
            if (pumpOn) temperature -= 0.2 + random.nextDouble() * 0.2;
            else temperature += 0.1 + random.nextDouble() * 0.3;
            temperature = Math.max(10, Math.min(temperature, 100));

            if (pumpOn) level -= 0.5 + random.nextDouble() * 0.5;
            else level += 0.1 + random.nextDouble() * 0.3;
            level = Math.max(0, Math.min(level, 100));

            if (valveOpen) pressure -= 0.05 + random.nextDouble() * 0.05;
            else pressure += 0.02 + random.nextDouble() * 0.05;
            pressure = Math.max(0.5, Math.min(pressure, 5.0));
        }
    }
}

