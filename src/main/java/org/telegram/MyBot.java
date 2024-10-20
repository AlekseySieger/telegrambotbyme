package org.telegram;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyBot extends TelegramLongPollingBot {

    private static final String OPENWEATHER_API_KEY = "tara-tara";

    @Override
    public String getBotUsername() {
        return "AlekseiSieger_bot";
    }

    @Override
    public String getBotToken() {
        return "88005553535:tara-ta-tara";
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (isValidCityName(messageText)) {
                String weatherResponse = getWeather(messageText.trim());
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(weatherResponse);

                try {

                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {

                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("ТЫ НАСТОЛЬКО ТУПОЙ, ЧТО НЕ МОЖЕШЬ НАПИСАТЬ ВЕРНОЕ НАЗВАНИЕ ГОРОДА ПО АНГЛИЙСКИ ?????");

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isValidCityName(String cityName) {

        return cityName != null && cityName.matches("^[a-zA-Z\\s]+$");
    }

    private String getWeather(String city) {
        String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + OPENWEATHER_API_KEY + "&units=metric";

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();

            JsonObject jsonObject = JsonParser.parseString(content.toString()).getAsJsonObject();

            if (jsonObject.has("cod") && jsonObject.get("cod").getAsInt() == 404) {
                return "Город не найден. Проверьте название.";
            }

            String cityName = jsonObject.get("name").getAsString();
            String weatherDescription = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();
            double temperature = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();

            return "Погода в " + cityName + ":\n" +
                    "Температура: " + temperature + "°C\n" +
                    "Описание: " + weatherDescription;

        } catch (Exception e) {
            return "Не удалось получить данные о погоде. Проверьте ваш запрос.";
        }
    }
}