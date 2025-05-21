import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.io.*;
import java.sql.*;

class GlobalVariables {
    /* Data Base SQL */
    public static final String DB_URL = "jdbc:sqlite:FitnessCenterProject.db";
    public static HashMap <String, String> userLoginRole = new HashMap<>();
    public static HashMap <String, ArrayList <String> > userRoleLogins = new HashMap<>();
    public static HashMap <String, String> userLoginPassword = new HashMap<>();
    public static HashMap <String, Integer> procedureTitleCost = new HashMap<>();
    public static ArrayList <String> procedureTitleLists = new ArrayList<>();
    public static HashMap <String, String> clientLoginNameAndSurname = new HashMap<>();

    /* Data Base TXT */
    public static ArrayList <String> procedureLists = new ArrayList<>();
    public static ArrayList <String> weekDays = new ArrayList<>();
    public static void insertDataToWeekDays(){
        weekDays.add("Понедельник");
        weekDays.add("Вторник");
        weekDays.add("Среда");
        weekDays.add("Четверг");
        weekDays.add("Пятница");
    }
    public static HashMap <String, ArrayList <String> > procedureSchedule = new HashMap<>();
    public static HashMap <String, ArrayList <String> > procedureTitleWeekDayAndTime = new HashMap<>();

    /* Data Base TXTv2 */
    public static HashMap <String, ArrayList <String> > paidProcedureLists = new HashMap<>();
    public static HashMap <String, String> paidProcedureLoginTimeAndDate = new HashMap<>();
    public static HashMap <String, ArrayList <String> > paidProcedureTitleLoginAndTimeAndDate = new HashMap<>();

    /* Data Base Time */
    public static String currentWeekDay, currentTime, currentDate;
    public static String currentDay, currentMonth, currentYear;

    /* Sign In */
    public static String currentRole, currentLogin;
}

class DataBaseSQL extends GlobalVariables {
    public static void dataBaseSQL(){
        createTable();

        if (factorySettings()){
            insertDataToUsers("personal", "p1", "p001");
            insertDataToUsers("director", "d1", "d001");
            insertDataToUsers("manager", "m1", "m001");
            insertDataToUsers("client", "c1", "c001");
            insertDataToUsers("client", "c2", "c002");

            insertDataToProcedures("Массаж", 3000);
            insertDataToProcedures("Йога", 2000);
            insertDataToProcedures("Бассейн", 4000);

            insertDataToClients("client1", "Мирлан", "Кыдыев", 182, 75, 3, "14.02.2007");
            insertDataToClients("client2", "Тагайбек", "Кубатов", 200, 150, 2, "20.10.2006");
        }

        readUsersData();
        readProceduresData();
        readClientsData();
    }

    private static void createTable() {
        String sqlUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                role TEXT NOT NULL,
                login TEXT NOT NULL,
                password TEXT NOT NULL
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
             stmt.execute(sqlUsers);

        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы: " + e.getMessage());
        }

        String sqlProcedures = """
            CREATE TABLE IF NOT EXISTS procedures (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                cost INTEGER NOT NULL
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
             stmt.execute(sqlProcedures);

        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы: " + e.getMessage());
        }

        String sqlClients = """
            CREATE TABLE IF NOT EXISTS clients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                login TEXT NOT NULL,
                name TEXT NOT NULL,
                surname TEXT NOT NULL,
                height INTEGER NOT NULL,
                weight INTEGER NOT NULL,
                bloodType INTEGER NOT NULL,
                dateOfBirth TEXT NOT NULL
           );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
             stmt.execute(sqlClients);

        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    private static boolean factorySettings() {
        String sql = "SELECT 1 FROM users LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return !rs.next();

        } catch (SQLException e) {
            System.out.println("Ошибка при работе с базой данных: " + e.getMessage());
        }

        return false;
    }

    public static void insertDataToUsers(String role, String login, String password) {
        String sql = "INSERT INTO users(role, login, password) VALUES(?, ?, ?)";

        if (userLoginRole.get(login) == null) {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, role);
                pstmt.setString(2, login);
                pstmt.setString(3, password);

                pstmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Ошибка при вставке данных: " + e.getMessage());
            }
        }
    }

    public static void insertDataToProcedures(String title, int cost) {
        String sql = "INSERT INTO procedures(title, cost) VALUES(?, ?)";

        if (procedureTitleCost.get(title) == null) {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, title);
                pstmt.setInt(2, cost);

                pstmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Ошибка при вставке данных: " + e.getMessage());
            }
        }
    }

    public static void insertDataToClients(String login, String name, String surname, Integer height, Integer weight, Integer bloodType, String dateOfBirth) {
        String sql = "INSERT INTO clients(login, name, surname, height, weight, bloodType, dateOfBirth) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, name);
            pstmt.setString(3, surname);
            pstmt.setInt(4, height);
            pstmt.setInt(5, weight);
            pstmt.setInt(6, bloodType);
            pstmt.setString(7, dateOfBirth);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Ошибка при вставке данных: " + e.getMessage());
        }
    }

    private static void readUsersData() {
        String sql = "SELECT * FROM users";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String role = rs.getString("role");
                String login = rs.getString("login");
                String password = rs.getString("password");

                userLoginRole.put(login, role);

                if (userRoleLogins.get(role) == null){
                    ArrayList <String> AL = new ArrayList<>();
                    AL.add(login);

                    userRoleLogins.put(role, AL);
                }
                else{
                    userRoleLogins.get(role).add(login);
                }

                userLoginPassword.put(login, password);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных: " + e.getMessage());
        }
    }

    private static void readProceduresData() {
        String sql = "SELECT * FROM procedures";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String title = rs.getString("title");
                int cost = rs.getInt("cost");

                procedureTitleCost.put(title, cost);
                procedureTitleLists.add(title);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных: " + e.getMessage());
        }
    }

    private static void readClientsData() {
        String sql = "SELECT * FROM clients";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String login = rs.getString("login");
                String name = rs.getString("name");
                String surname = rs.getString("surname");

                clientLoginNameAndSurname.put(login, name + " " + surname);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных: " + e.getMessage());
        }
    }
}

class DataBaseTXT extends GlobalVariables {
    public static void dataBaseTXT(){
        createFile();

        if (factorySettings()){
            writeFileToProcedureList("Массаж", "Понедельник", "13:00");
            writeFileToProcedureList("Йога", "Вторник", "15:00");
            writeFileToProcedureList("Бассейн", "Среда", "12:00");
            writeFileToProcedureList("Массаж", "Среда", "10:00");
            writeFileToProcedureList("Йога", "Четверг", "14:00");
            writeFileToProcedureList("Бассейн", "Пятница", "15:00");
            writeFileToProcedureList("Бассейн", "Вторник", "09:00");
        }
        else{
            readFile();
        }

        insertToProcedureSchedule();
        rewrite();
        insertToProcedureTitleWeekDayAndTime();
    }

    private static void createFile(){
        try {
            new File("procedureLists.txt").createNewFile();

        } catch (IOException e) {
            System.out.println("Ошибка при создании файла");
        }
    }

    private static boolean factorySettings(){
        return new File("procedureLists.txt").length() == 0;
    }

    public static void writeFileToProcedureList(String title, String weekDay, String time){
        procedureLists.add(title + " " + weekDay + " " + time);

        rewrite();
    }

    public static void rewrite(){
        try {
            FileWriter fileWriter = new FileWriter("procedureLists.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String x : procedureLists){
                bufferedWriter.write(x);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();

        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
        }
    }

    private static void readFile(){
        try {
            FileReader fileReader = new FileReader("procedureLists.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                procedureLists.add(line);
            }
            bufferedReader.close();

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла");
        }
    }

    private static void insertToProcedureSchedule(){
        GlobalVariables.insertDataToWeekDays();

        for (String x : weekDays) {
            ArrayList<String> list = new ArrayList<>();

            for (String y : procedureLists) {
                String[] list2 = y.split("\\s");

                if (list2[1].equals(x)){
                    list.add(list2[2] + " " + list2[0]);
                }
            }
            Collections.sort(list);
            ArrayList <String> list2 = new ArrayList<>();

            for (String y : list) {
                String[] list3 = y.split("\\s");
                list2.add(list3[1] + " " + list3[0]);
            }
            procedureSchedule.put(x, list2);
        }
    }

    private static void insertToProcedureTitleWeekDayAndTime(){
        try {
            FileReader fileReader = new FileReader("procedureLists.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] list = line.split("\\s");
                String title = list[0];
                String weekDay = list[1];
                String time = list[2];

                if (procedureTitleWeekDayAndTime.get(title) == null){
                    ArrayList <String> AL = new ArrayList<>();
                    AL.add(weekDay + " " + time);

                    procedureTitleWeekDayAndTime.put(title, AL);
                }
                else{
                    procedureTitleWeekDayAndTime.get(title).add(weekDay + " " + time);
                }
            }
            bufferedReader.close();

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла");
        }
    }
}

class DataBaseTXTv2 extends GlobalVariables {
    public static void dataBaseTXTv2(){
        for (String login : userRoleLogins.get("client")){
            createFile(login);

            if (factorySettings(login) && login.equals("c1")){
                writeFileToPaidProcedure(login, "Массаж", 3000, "Понедельник", "13:00", "2025.05.19");
                writeFileToPaidProcedure(login, "Бассейн", 4000, "Среда", "12:00", "2025.05.21");
                writeFileToPaidProcedure(login, "Йога", 2000, "Вторник", "15:00", "2025.05.20");
            }
            else{
                readFile(login);
            }
        }
    }

    private static void createFile(String login){
        try {
            new File(login + "PaidProcedures.txt").createNewFile();

        } catch (IOException e) {
            System.out.println("Ошибка при создании файла");
        }
    }

    private static boolean factorySettings(String login){
        return new File(login + "PaidProcedure.txt").length() == 0;
    }

    public static void writeFileToPaidProcedure(String login, String title, int cost, String weekDay, String time, String date){
        paidProcedureLists.get(login).add(title + " " + cost + " " + weekDay + " " + time + " " + date);
        paidProcedureLoginTimeAndDate.put(login, time + " " + date);
        paidProcedureTitleLoginAndTimeAndDate.get(title).add(login + " " + time + " " + date);

        try {
            FileWriter fileWriter = new FileWriter("procedureLists.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String x : paidProcedureLists.get(login)){
                bufferedWriter.write(x);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();

        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
        }
    }

    private static void readFile(String login) {
        try {
            FileReader fileReader = new FileReader(login + "PaidProcedures.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                paidProcedureLists.get(login).add(line);

                String title = line.split("\\s")[0];
                String time = line.split("\\s")[3];
                String date = line.split("\\s")[4];

                paidProcedureLoginTimeAndDate.put(login, time + " " + date);
                paidProcedureTitleLoginAndTimeAndDate.get(title).add(login + " " + time + " " + date);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла");
        }
    }
}

class DataBaseTime extends GlobalVariables {
    public static void dataBaseTime(){
        ZoneId zone = ZoneId.of("Asia/Bishkek");
        ZonedDateTime now = ZonedDateTime.now(zone);

        insertWeekDay(now);
        insertTimeAndData(now);
        insertDayMonthYear(now);
    }

    private static void insertWeekDay(ZonedDateTime now){
        String weekDay = now.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru"));
        char firstLetter = weekDay.toUpperCase().charAt(0);

        currentWeekDay = firstLetter + weekDay.substring(1);
    }

    private static void insertTimeAndData(ZonedDateTime now){
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        currentTime = now.format(time);
        currentDate = now.format(date);
    }

    private static void insertDayMonthYear(ZonedDateTime now){
        DateTimeFormatter day = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter month = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");

        currentDay = now.format(day);
        currentMonth = now.format(month);
        currentYear = now.format(year);
    }

    public static String insertDateToPaidProcedure(String paidProcedureTitleWeekDayAndTime){
        String weekDay = paidProcedureTitleWeekDayAndTime.split("\\s")[0];
        String time = paidProcedureTitleWeekDayAndTime.split("\\s")[1];

        int currentMinutes = 1440 * weekDayOrder(currentWeekDay);
        int minutes = 1440 * weekDayOrder(weekDay);

        currentMinutes += timeInMinutes(currentTime);
        minutes += timeInMinutes(time);

        int difference;

        if (currentMinutes >= minutes){
            difference = weekDayOrder(currentWeekDay) + 7 - weekDayOrder(weekDay);
        }
        else{
            difference = weekDayOrder(weekDay) - weekDayOrder(currentWeekDay);
        }

        return currentDateMethod(difference);
    }

    private static int weekDayOrder(String weekDay){
        return switch (weekDay) {
            case "Понедельник" -> 1;
            case "Вторник" -> 2;
            case "Среда" -> 3;
            case "Четверг" -> 4;
            case "Пятница" -> 5;
            default -> 0;
        };
    }

    private static int timeInMinutes(String time){
        String hour = time.substring(0, 2);
        String minute = time.substring(3);

        return Integer.parseInt(hour) * 60 + Integer.parseInt(minute);
    }

    private static String currentDateMethod(int difference){
        String year;
        String month;
        String day;

        int dayInt = Integer.parseInt(currentDay) + difference;

        if (countOfDaysInMonth() >= dayInt){
            year = currentYear;
            month = currentMonth;
            day = String.valueOf(dayInt);
        }
        else{
            if (currentMonth.equals("12")){
                year = String.valueOf(Integer.parseInt(currentYear) + 1);
                month = "01";
            }
            else{
                year = currentYear;
                month = String.valueOf(Integer.parseInt(currentMonth) + 1);
                if (month.length() == 1){
                    month = "0" + month;
                }
            }
            day = "0" + (countOfDaysInMonth() - dayInt);
        }

        return day + "." + month + "." + year;
    }

    private static int countOfDaysInMonth(){
        int currentMonthInt = Integer.parseInt(currentMonth);

        if (currentMonthInt == 2){
            return 28;
        }
        else if (currentMonthInt <= 7){
            return currentMonthInt % 2 == 0 ? 30 : 31;
        }
        else{
            return currentMonthInt % 2 == 0 ? 31 : 30;
        }
    }
}

public class Main extends GlobalVariables{
    public static void main(String[] args) {
        DataBaseMethodCall();
        SignInMethodCall();
        PDMC_MethodCall();

        System.exit(0);
    }

    private static void DataBaseMethodCall(){
        DataBaseSQL.dataBaseSQL();
        DataBaseTXT.dataBaseTXT();
        DataBaseTXTv2.dataBaseTXTv2();
        DataBaseTime.dataBaseTime();
    }

    private static void SignInMethodCall(){
        currentRole = SignIn.role();
        currentLogin = SignIn.login();
    }

    private static void PDMC_MethodCall(){
        /*
        switch (currentRole){
            case "personal":
                Personal.personal();
                break;
            case "director":
                Director.director();
                break;
            case "manager":
                Manager.manager();
                break;
            case "client":
                Client.client();
                break;
        }
         */
    }
}

class SignIn extends GlobalVariables {
    public static String role(){
        Scanner scan = new Scanner(System.in);

        boolean running = true;
        String role = "";

        while (running) {
            running = false;

            System.out.println("\npersonal/director/manager/client");
            System.out.print("Введите тип аккаунта: ");

            role = scan.next();

            switch (role){
                case "personal": break;
                case "director": break;
                case "manager": break;
                case "client": break;
                default:
                    running = true;
                    System.out.println("Неправильный тип аккаунта, повторите.");
            }
        }
        return role;
    }

    public static String login(){
        Scanner scan = new Scanner(System.in);

        System.out.print("Логин: ");
        String login = scan.next();

        System.out.print("Пароль: ");
        String password = scan.next();

        if (!loginPasswordProof(login, password)){
            System.out.println("\nНеправильный логин или пароль.");
            System.exit(0);
        }
        return login;
    }

    private static boolean loginPasswordProof(String login, String password){
        return userRoleLogins.get(currentRole).contains(login) && userLoginPassword.get(login).equals(password);
    }
}

class similarUserMethods extends DataBaseSQL {
    public static void anyKeyMethod(Boolean anyKey){
        Scanner scan = new Scanner(System.in);
        if (anyKey){
            System.out.print("\nНажмите любую клавишу чтобы продолжить. ");
            scan.nextLine();
        }
    }

    public static void listOfProcedures() {
        if (paidProcedureTitleLoginAndTimeAndDate.isEmpty()){
            System.out.println("\nНикто из посетителей не покупал процедуры.");
            return;
        }

        System.out.println("\nСписок процедур:");

        for (String title : procedureTitleLists){
            if (paidProcedureTitleLoginAndTimeAndDate.get(title) == null){
                continue;
            }

            System.out.println("------------");
            System.out.println(title + ":");

            for (String loginAndTimeAndDate : paidProcedureTitleLoginAndTimeAndDate.get(title)){
                System.out.println(loginAndTimeAndDate);
            }
        }
        System.out.println("------------");
    }

    public static void searchClientOrMyInfo(String login, Boolean search){
        Scanner scan = new Scanner(System.in);

        if (search){
            while (true) {
                System.out.print("\nВведите логин посетителя, которого хотите найти: ");
                login = scan.next();

                if (userRoleLogins.get("client").contains(login)){
                    break;
                }
                System.out.println("Посетитель с логином '" + login + "' не найден, повторите.");
            }
        }

        String query = "SELECT * FROM clients WHERE login = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, login);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()){
                    System.out.println("\nПосетитель найден:");

                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Имя: " + rs.getString("name"));
                    System.out.println("Фамилия: " + rs.getString("surname"));
                    System.out.println("Рост: " + rs.getInt("height") + " см");
                    System.out.println("Вес: " + rs.getInt("weight") + " кг");
                    System.out.println("Группа крови: " + rs.getInt("bloodType"));
                    System.out.println("Дата рождения: " + rs.getString("dateOfBirth"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске посетителя: " + e.getMessage());
        }
    }

    public static void allProcedures(){
        String sql = "SELECT * FROM procedures";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nВсе процедуры:");

            while (rs.next()) {
                String title = rs.getString("title");
                int cost = rs.getInt("cost");

                System.out.println(title + " - " + cost + ".00 сом");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных: " + e.getMessage());
        }
    }

    public static void procedureSchedule(){
        System.out.println("\nРасписание к процедурам:");

        for (String x : weekDays) {
            System.out.println("------------------------");
            System.out.println(x + ":");

            for (String y : procedureSchedule.get(x)) {
                System.out.println(y);
            }
        }
        System.out.println("------------------------");
    }

    public static void listOfClients(){
        String sql = "SELECT * FROM clients";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nСписок посетителей: ");

            while (rs.next()) {
                String login = rs.getString("login");
                String name = rs.getString("name");
                String surname = rs.getString("surname");

                System.out.println(login + " " + name + " " + surname);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных: " + e.getMessage());
        }
    }

    public static void clientPaymentHistory(String login, Boolean search){
        Scanner scan = new Scanner(System.in);

        while (search) {
            System.out.print("\nВведите логин посетителя, которого хотите найти: ");
            login = scan.next();

            if (userRoleLogins.get("client").contains(login)){
                search = false;
            }
            else {
                System.out.println("Посетитель с логином '" + login + "' не найден, повторите.");
            }
        }

        try {
            FileReader fileReader = new FileReader(login + "PaidProcedures.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;

            System.out.println("История оплаты посетителя " + login + ":");
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
            bufferedReader.close();

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла");
        }
    }
}

class Personal extends GlobalVariables {
    public static void personal(){
        Scanner scan = new Scanner(System.in);
        System.out.println("\nПриветствую уважаемый Персонал!");

        boolean running = true;
        boolean anyKey = false;

        while (running){
            similarUserMethods.anyKeyMethod(anyKey);
            anyKey = true;

            System.out.println("\nМеню персонала:");

            System.out.println("1. Показать список процедур.");
            System.out.println("2. Найти посетителя.");
            System.out.println("3. Показать все процедуры.");
            System.out.println("4. Показать расписание к процедурам.");
            System.out.println("5. Купить процедуру.");
            System.out.println("6. Найти процедуры.");
            System.out.println("0. Выход.");

            System.out.print("Выбор: ");

            switch (scan.next()) {
                case "1":
                    similarUserMethods.listOfProcedures();
                    break;
                case "2":
                    similarUserMethods.searchClientOrMyInfo("", true);
                    break;
                case "3":
                    similarUserMethods.allProcedures();
                    break;
                case "4":
                    similarUserMethods.procedureSchedule();
                    break;
                case "5":
                    buyProcedure();
                    break;
                case "6":
                    findProcedure();
                    break;
                case "0":
                    running = false;
                    System.out.println("\nПрограмма завершена, мы будем рады вашему возвращению!");
                    break;
                default:
                    System.out.println("\nНеверный выбор, повторите.");
            }
        }
    }

    private static void buyProcedure(){
        Scanner scan = new Scanner(System.in);
        String login;

        while (true) {
            System.out.print("\nВведите логин посетителя, которому хотите купить процедуру: ");
            login = scan.next();

            if (userRoleLogins.get("client").contains(login)){
                break;
            }
            System.out.println("Посетитель с логином '" + login + "' не найден, повторите.");
        }

        String title;

        while (true){
            System.out.print("\nВведите процедуру, которую хотите купить посетителю: ");
            title = scan.next();

            if (procedureTitleCost.get(title) != null){
                break;
            }
            System.out.println("Процедура с названием '" + title + "' не найден, повторите.");
        }

        String paidProcedureTitleWeekDayAndTimeAndDate;

        while (true){
            System.out.println("\nРасписание процедуры " + title + ":");

            int index = 0;
            for (String x : procedureTitleWeekDayAndTime.get(title)){
                System.out.println(++index + ". " + x);
            }
            System.out.print("Ваш выбор: ");
            int choose = scan.nextInt();

            if (choose < 1 || choose > index){
                System.out.println("Неправильный выбор, повторите.");
            }
            else{
                paidProcedureTitleWeekDayAndTimeAndDate = procedureTitleWeekDayAndTime.get(title).get(index-1);
                break;
            }
        }

        paidProcedureTitleWeekDayAndTimeAndDate += " " + DataBaseTime.insertDateToPaidProcedure(paidProcedureTitleWeekDayAndTimeAndDate);

        if (paidProcedureLists.get(login) == null){
            ArrayList <String> AL = new ArrayList<>();
            AL.add(title + " " + procedureTitleCost.get(title) + " " + paidProcedureTitleWeekDayAndTimeAndDate);

            paidProcedureLists.put(login, AL);
        }
        else{
            paidProcedureLists.get(login).add(title + " " + procedureTitleCost.get(title) + " " + paidProcedureTitleWeekDayAndTimeAndDate);
        }

        if (paidProcedureTitleLoginAndTimeAndDate.get(title) == null){
            ArrayList <String> AL = new ArrayList<>();
            AL.add(login + paidProcedureLoginTimeAndDate.get(login));

            paidProcedureTitleLoginAndTimeAndDate.put(login, AL);
        }
        else{
            paidProcedureTitleLoginAndTimeAndDate.get(title).add(login + paidProcedureLoginTimeAndDate.get(login));
        }

        try {
            FileWriter fileWriter = new FileWriter(login + "PaidProcedures.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String x : paidProcedureLists.get(login)){
                bufferedWriter.write(x);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();

        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
        }

        System.out.println("Покупка процедуры прошла успешно!");
    }

    private static void findProcedure(){
        Scanner scan = new Scanner(System.in);
        String title;

        while (true) {
            System.out.print("\nВведите название процедуры: ");
            title = scan.next();

            if (procedureTitleCost.get(title) != null){
                break;
            }
            System.out.println("Процедура с названием '" + title + "' не найден, повторите.");
        }

        System.out.println("Стоимость процедуры: " + procedureTitleCost.get(title) + ".00 сом");
        System.out.println("Количество записанных посетителей: " + paidProcedureTitleLoginAndTimeAndDate.get(title).size());

    }
}