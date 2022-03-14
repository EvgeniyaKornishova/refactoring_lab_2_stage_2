package ru.ifmo.calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static ru.ifmo.calculator.Main.Operator.*;

public class Main {
    enum Operator{
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE
    }

    enum Error{
        CONNECTION_ERROR("Server connection error"),
        INVALID_RESULT_ID_FORMAT_ERROR("Invalid result ID format"),
        UNKNOWN_RESULT_ID_ERROR("Unknown result ID"),
        UNKNOWN_OPERATOR_ERROR("Unknown operator"),
        INVALID_NUMBER_FORMAT_ERROR("Invalid number format error"),
        ZERO_DIVISION_ERROR("Zero division error"),
        USERNAME_ALREADY_EXISTS("Username already exists"),
        UNAUTHORIZED("Login or password incorrect")
        ;
        private final String text;

        /**
         * @param text
         */
        Error(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

    private static final String EXIT_COMMAND = "q";
    private static final String API_BASE_URL = "http://localhost:5080";
    private static final String RESULTS_API_URL = API_BASE_URL + "/results/";
    private static final String AUTH_URL = API_BASE_URL + "/login";
    private static final String REGISTER_URL = API_BASE_URL + "/register";

    private static void printUsage() {
        System.out.println(
                "Usage:\r\n" +
                        "\twhen a first symbol on line is '>' – enter operand (number)\r\n" +
                        "\twhen a first symbol on line is '@' – enter operation\r\n" +
                        "\t\toperation is one of '+', '-', '/', '*' or\r\n" +
                        "\t\t'#' followed with number of evaluation step\r\n" +
                        "\t'q' to exit"
        );
    }

    private static void printResult(Integer resultId, Float result){
        System.out.println("[#" + resultId + "]=" + result);
    }

    private static void printError(Error msg){
        System.out.println("ERROR: " + msg + "!!!");
    }

    private static Integer sendResult(String cookie, Float result) throws ConnectException{
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(RESULTS_API_URL))
                    .headers("Content-Type", "application/json;charset=UTF-8", "Authorization", cookie)
                    .POST(HttpRequest.BodyPublishers.ofString("{\"value\":"+result+"}"))
                    .build();
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            throw new ConnectException();
        }

        return Integer.parseInt(response.body());
    }

    private static Float getResult(String cookie, Integer resultId) throws ConnectException, IllegalArgumentException{
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(RESULTS_API_URL  + resultId))
                    .headers("Authorization", cookie)
                    .GET()
                    .build();
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            throw new ConnectException();
        }

        if (response.statusCode() == 404) {
            throw new IllegalArgumentException();
        }

        return Float.parseFloat(response.body());
    }

    private static float calc(float x, float y, Operator op) throws ArithmeticException{
        if (op == null)
            return y;

            switch (op) {
                case PLUS:
                    return x + y;
                case MINUS:
                    return x - y;
                case MULTIPLY:
                    return x * y;
                case DIVIDE:
                    if (y == 0)
                        throw new ArithmeticException();
                    return x / y;
            }
        return 0;
    }

    private static String auth(String login, String password) throws ConnectException {
        String cookie = "Basic " + Base64.getEncoder().encodeToString((login + ":" + password).getBytes());

        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(AUTH_URL))
                    .headers("Authorization", cookie)
                    .GET()
                    .build();
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            throw new ConnectException();
        }

        if (response.statusCode() == 401){
            printError(Error.UNAUTHORIZED);
            return null;
        }

       return cookie;
    }

    private static void register(String login, String password) throws ConnectException {
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(REGISTER_URL))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"" + login + "\", \"password\":\"" + password + "\"}"))
                    .build();
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        }catch (Exception e){
            throw new ConnectException();
        }

        if (response.statusCode() == 409){
            printError(Error.USERNAME_ALREADY_EXISTS);
            return;
        }

        System.out.println("Account created");
    }

    private static String authentication_dialog(BufferedReader reader) throws IOException {
       // select auth or register
       String input;
       Integer choice = 0;
       String login;
       String password;

       do {
           System.out.println("Log in(1) or Register(2): ");
           input = reader.readLine();

           try {
               choice = Integer.parseInt(input);
           } catch (NumberFormatException ignored){}
       } while(choice != 1 && choice != 2);

       System.out.println("login: ");
       login = reader.readLine();
       System.out.println("password: ");
       password = reader.readLine();

       if (choice == 1)
           return auth(login, password);
       else
           register(login, password);

       return null;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // authentication
        String cookie = null;
        while (cookie == null){
            cookie = authentication_dialog(reader);
        }

        printUsage();

        String input;
        boolean input_type_operation = false;

        float result = 0;
        float operand;
        int operationId;
        Operator op = null;

        do{
            // Print greeting
            System.out.print(input_type_operation ? "@" : ">");

            input = reader.readLine();
            if (input.equals(EXIT_COMMAND))
                continue;

            if (input_type_operation){
               switch (input){
                   case "+": op = PLUS; break;
                   case "-": op = MINUS; break;
                   case "*": op = MULTIPLY; break;
                   case "/": op = DIVIDE; break;
                   default:
                       if (input.startsWith("#")){
                           try{
                               operationId = Integer.parseInt(input.substring(1));
                           }catch (NumberFormatException e){
                               printError(Error.INVALID_RESULT_ID_FORMAT_ERROR);
                               continue;
                           }

                           try {
                               result = getResult(cookie, operationId);
                           }catch (ConnectException e){
                               printError(Error.CONNECTION_ERROR);
                               continue;
                           }catch (IllegalArgumentException e){
                               printError(Error.UNKNOWN_RESULT_ID_ERROR);
                               continue;
                           }

                           try {
                               operationId = sendResult(cookie, result);
                           }catch (ConnectException e){
                               printError(Error.CONNECTION_ERROR);
                               continue;
                           }

                           printResult(operationId, result);
                       }else
                           printError(Error.UNKNOWN_OPERATOR_ERROR);
                       continue;
               }
            }else{
                try {
                    operand = Float.parseFloat(input);
                }catch (NumberFormatException e){
                    printError(Error.INVALID_NUMBER_FORMAT_ERROR);
                    continue;
                }

                try {
                    result = calc(result, operand, op);
                }catch (ArithmeticException e){
                    printError(Error.ZERO_DIVISION_ERROR);
                    continue;
                }

                try{
                    operationId = sendResult(cookie, result);
                }catch (ConnectException e){
                    printError(Error.CONNECTION_ERROR);
                    continue;
                }

                printResult(operationId, result);
            }

            // change next step input type
            input_type_operation = !input_type_operation;
        }while(!input.equals(EXIT_COMMAND));
    }

}
