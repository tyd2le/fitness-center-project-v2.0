import java.util.*;
import java.sql.*;
import java.io.*;

class GlobalVariables {
    public static HashMap <String, String> userLoginRole = new HashMap<>();

    public static HashMap <String, ArrayList <String> > userRoleLogin = new HashMap<>();

    public static HashMap <String, String> userLoginPassword = new HashMap<>();

    public static HashMap <String, Integer> procedureTitleCost = new HashMap<>();

    public static HashMap <String, Integer> clientLoginId = new HashMap<>();

    public static final String DB_URL = "jdbc:sqlite:FitnessCenterProject.db";

    public static ArrayList <String> procedureLists = new ArrayList<>();

    public static HashMap <String, ArrayList <String> > procedureSchedule = new HashMap<>();

    public static ArrayList <String> weekDays = new ArrayList<>();

    public static void insertDataToWeekDays(){
        weekDays.add("Понедельник");
        weekDays.add("Вторник");
        weekDays.add("Среда");
        weekDays.add("Четверг");
        weekDays.add("Пятница");
    }

    public static String currentRole, currentLogin;

    public static HashMap <String, ArrayList <String> > paidProcedureList = new HashMap<>();
}

class insertDataMethods extends DataBaseSQL {
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

        if (clientLoginId.get(login) == null) {
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
    }
}

class DataBaseSQL extends GlobalVariables {
    public static void dataBaseSQL(){

        /* deleteTable(); */ createTable();

        if (factorySettings()) {
            insertDataMethods.insertDataToUsers("personal", "personal1", "pel1");
            insertDataMethods.insertDataToUsers("director", "director1", "dir1");
            insertDataMethods.insertDataToUsers("manager", "manager1", "mar1");
            insertDataMethods.insertDataToUsers("client", "client1", "clt1");

            insertDataMethods.insertDataToProcedures("Массаж", 2000);
            insertDataMethods.insertDataToProcedures("Йога", 5000);
            insertDataMethods.insertDataToProcedures("Бассейн", 4000);

            insertDataMethods.insertDataToClients("client1", "Мирлан", "Кыдыев", 182, 75, 3, "14.02.2007");
            insertDataMethods.insertDataToClients("client2", "Тагайбек", "Кубатов", 200, 150, 2, "20.10.2006");
        }

        readUsersData();
        readProceduresData();
        readClientsData();
    }

    private static void deleteTable() {
        ArrayList <String> tableName = new ArrayList<>();

        tableName.add("users");
        tableName.add("procedures");
        tableName.add("clients");

        for (String x : tableName) {
            String sql = "DROP TABLE IF EXISTS " + x;

            try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
                stmt.execute(sql);

            } catch (SQLException e) {
                System.err.println("Ошибка при удалении таблицы: " + e.getMessage());
            }
        }
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

                if (userRoleLogin.get(role) != null){
                    userRoleLogin.get(role).add(login);
                }
                else{
                    ArrayList <String> AL = new ArrayList<>();
                    AL.add(login);

                    userRoleLogin.put(role, AL);
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

                clientLoginId.put(login, id);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных: " + e.getMessage());
        }
    }
}

class DataBaseTXT extends GlobalVariables {
    public static void dataBaseTXT(){

        /* deleteFile(); */ createFile();

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

        rewriteFile();
    }

    private static void deleteFile(){
        new File("procedureList.txt").delete();
    }

    private static void createFile(){
        try {
            new File("procedureList.txt").createNewFile();

        } catch (IOException e) {
            System.out.println("Ошибка при создании файла");
        }
    }

    private static boolean factorySettings(){
        return new File("procedureList.txt").length() == 0;
    }

    public static void writeFileToProcedureList(String procedureName, String weekDay, String time){
        String newProcedureList = procedureName + " " + weekDay + " " + time;

        if (procedureLists.contains(newProcedureList)){
            System.out.println("Произошло повторение");
            return;
        }
        procedureLists.add(newProcedureList);

        try {
            FileWriter fileWriter = new FileWriter("procedureList.txt");
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
            FileReader fileReader = new FileReader("procedureList.txt");
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
                String[] weekDay = y.split("\\s+");

                if (weekDay[1].equals(x)){
                    if (factorySettings()) {
                        list.add(weekDay[2] + " " + weekDay[0]);
                    }
                    else{
                        list.add(weekDay[0] + " " + weekDay[2]);
                    }
                }
            }

            if (!factorySettings()){
                procedureSchedule.put(x, list);
                continue;
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

    private static void rewriteFile(){
        try {
            FileWriter fileWriter = new FileWriter("procedureList.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String x : weekDays) {
                for (String y : procedureSchedule.get(x)) {
                    bufferedWriter.write(x + " " + y);
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.close();

        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
        }
    }
}

class DataBaseTXTv2 extends GlobalVariables {
    public static void dataBaseTXTv2(String login){

        /* deleteFile(login) */ createFile(login);
    }

    private static void deleteFile(String login){
        new File(login + "_paid_procedure.txt").delete();
    }

    private static void createFile(String login){
        try {
            new File(login + "_paid_procedure.txt").createNewFile();

        } catch (IOException e) {
            System.out.println("Ошибка при создании файла");
        }
    }
}

public class Main extends GlobalVariables{
    public static void main(String[] args){
        DataBaseSQL.dataBaseSQL();

        DataBaseTXT.dataBaseTXT();

        for (String x : userRoleLogin.get("client")) {
            DataBaseTXTv2.dataBaseTXTv2(x);
        }

        SignIn.role();

        SignIn.login();

        switch (currentRole){
            case "personal":
                Personal.personal();
                break;
        }
    }
}

class SignIn extends GlobalVariables{
    public static void role(){
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
        currentRole = role;
    }

    public static void login(){
        Scanner scan = new Scanner(System.in);

        System.out.print("Логин: ");
        String login = scan.next();

        System.out.print("Пароль: ");
        String password = scan.next();

        if (!loginPasswordProof(login, password)){
            System.out.println("Неправильный логин или пароль.");
            System.exit(0);
        }
        currentLogin = login;
    }

    private static boolean loginPasswordProof(String login, String password){
        return userRoleLogin.get(currentRole).contains(login) && userLoginPassword.get(login).equals(password);
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

    }

    public static void searchClientOrMyInfo(String login, Boolean search){
        Scanner scan = new Scanner(System.in);

        if (search){
            while (true) {
                System.out.print("\nВведите логин посетителя, которого хотите найти: ");
                login = scan.next();

                if (clientLoginId.get(login) != null){
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
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String surname = rs.getString("surname");
                    int height = rs.getInt("height");
                    int weight = rs.getInt("weight");
                    int bloodType = rs.getInt("bloodType");
                    String dateOfBirth = rs.getString("dateOfBirth");

                    System.out.println("\nПосетитель найден:");
                    System.out.println("ID: " + id);;
                    System.out.println("Имя: " + name);
                    System.out.println("Фамилия: " + surname);
                    System.out.println("Рост: " + height + " см");
                    System.out.println("Вес: " + weight + " кг");
                    System.out.println("Группа крови: " + bloodType);
                    System.out.println("Дата рождения: " + dateOfBirth);
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
            System.out.println(x);

            for (String y : procedureSchedule.get(x)) {
                System.out.println(y);
            }
        }
    }
}

class Personal extends GlobalVariables{
    public static void personal(){
        Scanner scan = new Scanner(System.in);
        System.out.println("\nПриветствую уважаемый Персонал!");

        boolean running = true;
        boolean anyKey = false;

        while (running){
            similarUserMethods.anyKeyMethod(anyKey);
            anyKey = true;

            System.out.println("\nМеню персонала:");

            System.out.println("1. Показать список процедур");
            System.out.println("2. Найти посетителя");
            System.out.println("3. Показать все процедуры");
            System.out.println("4. Показать расписание к процедурам");
            System.out.println("5. Купить процедуру");
            System.out.println("6. Найти процедуры");
            System.out.println("0. Выход");

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

            if (clientLoginId.get(login) != null){
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
            System.out.println("Процедура с названием '" + title + "' не найдено, повторите.");
        }

        if (paidProcedureList.get(login) == null){
            ArrayList <String> AL = new ArrayList<>();
            AL.add(title + " " + procedureTitleCost.get(title));

            paidProcedureList.put(login, AL);
        }
        else{
            paidProcedureList.get(login).add(title + " " + procedureTitleCost.get(title));
        }

        try {
            FileWriter fileWriter = new FileWriter(login + "_paid_procedure.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String x : paidProcedureList.get(login)){
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

    }
}
