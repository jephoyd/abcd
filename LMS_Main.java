import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
//import java.nio.charset.*;

//----------------------[GLOBAL VARIABLES]----------------------
class Vars{
    public static final String LMS_FOLDER_DB = ".\\LMS Database";
    public static final String LMS_LOGS = LMS_FOLDER_DB + "\\LMS Logs";
    public static final String LMS_ACCOUNTS = LMS_FOLDER_DB + "\\ACCOUNTS.csv";
    public static final String LMS_BOOKS = LMS_FOLDER_DB + "\\BOOKS.txt";
    public static final String LMS_BOOK_REQUESTS = LMS_FOLDER_DB + "\\Book_Requests.txt";
    public static final String LMS_KEY = LMS_FOLDER_DB + "\\KEY.txt";
    
    public static final String LMS_CHCEK_FD = "D:\\LMS_Check.txt";
    public static final String LMS_FLASH_DRIVE = "D:\\LMS Details";
    public static final String LMS_FD_USER_DETAILS = LMS_FLASH_DRIVE + "\\USER_DETAILS.csv";
    public static final String LMS_FD_BORROWED_BOOKS = LMS_FLASH_DRIVE + "\\BORROWED_BOOKS.txt";
    
    public static final String FILL = "NONE";
    public static final String ADMIN_FILL_NAME = "LibrarianADMIN";
    public static final String ADMIN_FILL_ID = "LibrarianADMIN123";
    
    public static int CURRENT_S_KEY, CURRENT_L_KEY, CURRENT_VIOLATION;
    public static String CURRENT_STUDENT_ID, CURRENT_STUDENT_NAME;
    public static String CURRENT_LIBRARIAN_ID, CURRENT_LIBRARIAN_NAME;
    public static boolean KEY_EXIST;
}

public class LMS_Main {
    private static LinkedList<AccountDetails> accountList = new LinkedList<>();
    private static LinkedList<BookDetails> bookList = new LinkedList<>();
    private static LinkedList<BookDetails> bookRquestList = new LinkedList<>();
    private static int KEY;
    private static Scanner scan = new Scanner(System.in);
    private static Scanner pause = new Scanner(System.in);

    public static void main(String[] args) {
        int userNum;

        //create directory for the Database and Logs
        File directoryDB = new File(Vars.LMS_FOLDER_DB);
        File directoryLogs = new File(Vars.LMS_LOGS);
        directoryDB.mkdir();
        directoryLogs.mkdir();

        //Create instance of class to call non-static methods from main
        LMS_Main obj = new LMS_Main();

        switch (obj.insertcard()){
            case 1: cls();
                    obj.registerStudent();
                    saveAccountFD();
                    saveAccounts();
                    logs("IN", Vars.FILL);
                    break;
            case 2: cls();
                    obj.registerLibrarian();
                    saveAccountFD();
                    saveAccounts();
                    logs("IN", Vars.FILL);
                    break;
            case 3: cls();
                    exitMessage();
                    System.exit(0);
                    break;
            case 4: cls();
                    userLOGIN();
                    break;
            default: cls();
                     System.out.println("INVALID INPUT.");
                     pause();
                     cls();
                     break;
        }

        obj.retrieveBooks();
        if (Vars.KEY_EXIST == false) {
            //get temporary key if there is no key yet
            //so if the user save and exit the program
            //the data will still be encrypted.
            getKey();

            //check if the files are existed
            //if existed save the data to the new key
            File fp = new File(Vars.LMS_ACCOUNTS);
            if (fp.exists()) {
                saveAccounts();
            }
            fp = new File(Vars.LMS_FD_USER_DETAILS);
            if (fp.exists()) {
                saveAccountFD();
            } 
            fp = new File(Vars.LMS_BOOKS);
            if (fp.exists()) {
                saveBooks();
            }
            fp = new File(Vars.LMS_BOOK_REQUESTS);
            if (fp.exists()) {
                saveBookRequests();
            }
        }
        cls();

        //student
        if (Vars.CURRENT_S_KEY == 1 && Vars.CURRENT_L_KEY == 0){ 
            while (true){
                cls();
                switch (menu(1)){
                    case 1: cls();
                            displayBooks();
                            pause();
                            break;
                    case 2: cls();
                            displayBooks();
                            borrowBook();
                            break;
                    case 3: cls();
                            obj.returnBook();
                            break;
                    case 4: cls();
                            obj.requestBook();
                            break;
                    case 5: cls();
                            viewViolation();
                            break;
                    case 6: cls();
                            exitMessage();
                            saveAccounts();
                            saveAccountFD();
                            saveBooks();
                            logs("OUT", Vars.FILL);
                            System.exit(0);
                            break;
                    default: cls();
                             System.out.println("INVALID INPUT.");
                             pause();
                             cls();
                             break;
                }
            }
        }
        //librarian
        else if (Vars.CURRENT_S_KEY == 0 && Vars.CURRENT_L_KEY == 1){ 
            while (true){
                cls();
                switch (menu(2)){
                    case 1: cls();
                            displayBooks();
                            userNum = prompts(2);
                            switch (userNum) {
                                case 1 -> insertExistingBook();
                                case 2 -> obj.insertNewBook();
                                default -> {
                                    System.out.printf("%n%nCANCELLED%n");
                                    pause();
                                    cls();
                                }
                            }
                            break;
                    case 2: cls();
                            displayBooks();
                            editBooks();
                            break;
                    case 3: cls();
                            displayBooks();
                            userNum = prompts(3);
                            switch (userNum) {
                                case 1 -> removeAcopy();
                                case 2 -> removeBooks();
                                default -> {
                                    System.out.printf("%n%nCANCELLED%n");
                                    pause();
                                    cls();
                                }
                            }
                            break;
                    case 4: cls();
                            obj.bookRequests();
                            break;
                    case 5: cls();
                            changeKey();
                            saveAccounts();
                            saveAccountFD();
                            saveBooks();
                            saveBookRequests();
                            break;
                    case 6: cls();
                            displayBooks();
                            break;
                    case 7: cls();
                            displayLogs();
                            break;
                    case 8: cls();
                            exitMessage();
                            saveAccounts();
                            saveAccountFD();
                            saveBooks();
                            logs("OUT", Vars.FILL);
                            System.exit(0);
                            break;
                    default: cls();
                            System.out.println("INVALID INPUT.");
                            pause();
                            cls();
                            break;
                }
            }
        }
    }

    //----------------------[LinkedList Structures]----------------------

    class AccountDetails {
        private  int skey, lkey, violation;
        private String studentID, studentName;
        private String librarianID, librarianName;

        //public method to add accounts
        public AccountDetails (String studentID, String librarianID,
                               String studentName, String librarianName,
                              int skey, int lkey, int violation){
            this.studentID = studentID;
            this.librarianID = librarianID;
            this.studentName = studentName;
            this.librarianName = librarianName;
            this.skey = skey;
            this.lkey = lkey;
            this.violation = violation;
        }

        //public methods to access and display the accounts
        public String getStudentID() {
            return studentID;
        }
        public String getLibrarianID() {
            return librarianID;
        }
        public String getStudentName() {
            return studentName;
        }
        public String getLibrarianName() {
            return librarianName;
        }
        public int getSkey() {
            return skey;
        }
        public int getLkey() {
            return lkey;
        }
        public int getViolation() {
            return violation;
        }

        //public methods to edit the violation of the account
        public void setViolation(int violation){
            this.violation = violation;
        }
    }

    class BookDetails {
        private int bookNum, publicationYear, bookQuantity;
        private String bookTitle, bookAuthor, ISBN;

        //public method to add books
        public BookDetails (int bookNum, String ISBN, String bookTitle,
                            String bookAuthor, int publicationYear, int bookQuantity){
            this.bookNum = bookNum;
            this.ISBN = ISBN;
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
            this.publicationYear = publicationYear;
            this.bookQuantity = bookQuantity;
        }

        //public methods to access and display the books
        public int getBookNum() {
            return bookNum;
        }
        public String getISBN() {
            return ISBN;
        }
        public String getBookTitle() {
            return bookTitle;
        }
        public String getBookAuthor() {
            return bookAuthor;
        }
        public int getPublicationYear() {
            return publicationYear;
        }
        public int getBookQuantity() {
            return bookQuantity;
        }

        //public methods to edit the details of the book
        public void setBookNum(int bookNum) {
            this.bookNum = bookNum;
        }
        public void setBookISBN(String ISBN) {
            this.ISBN = ISBN;
        }
        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }
        public void setBookAuthor(String bookAuthor) {
            this.bookAuthor = bookAuthor;
        }
        public void setBookPubYear(int pubyear) {
            this.publicationYear = pubyear;
        }
        public void setBookQuantity(int bookQuantity) {
            this.bookQuantity = bookQuantity;
        }
    }

    //----------------------[LOG IN]----------------------
    public int insertcard() {
        int userNum;
        boolean isValid = false;

        //check if card is inserted
        File fp = new File(Vars.LMS_CHCEK_FD);
        while (!fp.exists()) {
            try {
                fp.createNewFile();
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            loading();
            cls();
        }
        File directoryFD = new File(Vars.LMS_FLASH_DRIVE);
        directoryFD.mkdir();
        front();

        fp = new File(Vars.LMS_FD_USER_DETAILS);
        if (fp.exists()) {
            retrieveKEY();
            retrieveAccounts();
            scanScreen(1);
            return 4;
        } else {
            retrieveKEY();
            retrieveAccounts();
            scanScreen(2);
            while (!isValid) {
                cls();
                System.out.println("NEW USER");
                System.out.println("CHOOSE CATEGORY: ");
                System.out.printf("%n[1] STUDENT");
                System.out.printf("%n[2] LIBRARIAN");
                System.out.printf("%n[3] CANCEL");
                System.out.printf("%n%n-> ");
                try {
                    userNum = scan.nextInt();
                    scan.nextLine(); // Consume the newline character
                    if (userNum > 0 && userNum <= 3) {
                        return userNum;
                    } else {
                        System.out.println("INVALID INPUT.");
                        System.out.println("PLEASE TRY AGAIN");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("ERROR" + e.getMessage());
                }
            }
            return 0;
        }
    }

    public static void retrieveKEY() {
        //get the key if meron
        File fkey = new File(Vars.LMS_KEY);
        if (fkey.exists()){
            try(BufferedReader readKey = new BufferedReader(new FileReader(fkey))) {
                String strKey;

                strKey = readKey.readLine();
                KEY = Integer.parseInt(strKey);

                Vars.KEY_EXIST = true;
            } catch (IOException e){
                System.out.println("ERROR: " + e.getMessage());
            }
        }else {
            Vars.KEY_EXIST = false;
        }
    }

    public void retrieveAccounts() {
        //retrieve accounts in database
        File fp = new File(Vars.LMS_ACCOUNTS);
        if (!fp.exists()){
            try{
                fp.createNewFile();
            }catch (IOException e){
                System.out.println("ERROR: " + e.getMessage());
            }
        }else{
            try(BufferedReader f2p = new BufferedReader(new FileReader(fp))){
                String str;

                while((str = f2p.readLine()) != null){
                    String[] tokens = str.split(",");
                    
                    if (Vars.KEY_EXIST == false){
                        //scan the plain text in the file because there is no key yet
                        String studentID = tokens[0];
                        String librarianID = tokens[1];
                        String studentName = tokens[2];
                        String librarianName = tokens[3];
                        int skey = Integer.parseInt(tokens[4]);
                        int lkey = Integer.parseInt(tokens[5]);
                        int violation = Integer.parseInt(tokens[6]);

                        //-------------------add the decrypted value to list-------------------
                        AccountDetails accounts = new AccountDetails(studentID, librarianID, studentName, librarianName, skey, lkey, violation);
                        accountList.add(accounts);
                    }else{
                        //-------------------display temporarily-------------------
                        System.out.println("---------ENCRYPTED---------");
                        System.out.println("studentID: " + tokens[0]);
                        System.out.println("librarianID: " + tokens[1]);
                        System.out.println("studentName: " + tokens[2]);
                        System.out.println("librarianName: " + tokens[3]);
                        System.out.println("skey: " + tokens[4]);
                        System.out.println("lkey: " + tokens[5]);
                        System.out.println("violation: " + tokens[6]);
                        System.out.println("---------ENCRYPTED---------");

                        //-------------------decrypting-------------------
                        String studentID = decryptString(tokens[0], KEY);
                        String librarianID = decryptString(tokens[1], KEY);
                        String studentName = decryptString(tokens[2], KEY);
                        String librarianName = decryptString(tokens[3], KEY);
                        int skey = decryptInt(Integer.parseInt(tokens[4]), KEY);
                        int lkey = decryptInt(Integer.parseInt(tokens[5]), KEY);
                        int violation = decryptInt(Integer.parseInt(tokens[6]), KEY);

                        //-------------------display temporarily-------------------
                        System.out.println("---------DECRYPTED---------");
                        System.out.println("studentID: " + studentID);
                        System.out.println("librarianID: " + librarianID);
                        System.out.println("studentName: " + studentName);
                        System.out.println("librarianName: " + librarianName);
                        System.out.println("skey: " + skey);
                        System.out.println("lkey: " + lkey);
                        System.out.println("violation: " + violation);
                        System.out.println("---------DECRYPTED---------");

                        //-------------------add the decrypted value to list-------------------
                        AccountDetails accounts = new AccountDetails(studentID, librarianID, studentName, librarianName, skey, lkey, violation);
                        accountList.add(accounts);
                    }
                }
            }catch (FileNotFoundException e){
                System.out.println("ERROR: " + e.getMessage());
            }catch (IOException e){
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        //retrieve account details from flashdrive
        fp = new File(Vars.LMS_FD_USER_DETAILS);
        if(fp.exists()){
            try(BufferedReader f2p = new BufferedReader(new FileReader(fp))){
                String str;

                while((str = f2p.readLine()) != null){
                    String[] tokens = str.split(",");

                    if (Vars.KEY_EXIST == false){
                        Vars.CURRENT_STUDENT_ID = tokens[0];
                        Vars.CURRENT_LIBRARIAN_ID = tokens[1];
                        Vars.CURRENT_STUDENT_NAME = tokens[2];
                        Vars.CURRENT_LIBRARIAN_NAME = tokens[3];
                        Vars.CURRENT_S_KEY = Integer.parseInt(tokens[4]);
                        Vars.CURRENT_L_KEY = Integer.parseInt(tokens[5]);
                        Vars.CURRENT_VIOLATION = Integer.parseInt(tokens[6]);
                    }else {
                        //-------------------display temporarily-------------------
                        System.out.println("---------ENCRYPTED---------");
                        System.out.println("curr-studentID: " + tokens[0]);
                        System.out.println("curr-librarianID: " + tokens[1]);
                        System.out.println("curr-studentName: " + tokens[2]);
                        System.out.println("curr-librarianName: " + tokens[3]);
                        System.out.println("curr-skey: " + tokens[4]);
                        System.out.println("curr-lkey: " + tokens[5]);
                        System.out.println("curr-violation: " + tokens[6]);
                        System.out.println("---------ENCRYPTED---------\n");

                        //-------------------decrypting-------------------
                        Vars.CURRENT_STUDENT_ID = decryptString(tokens[0], KEY);
                        Vars.CURRENT_LIBRARIAN_ID = decryptString(tokens[1], KEY);
                        Vars.CURRENT_STUDENT_NAME = decryptString(tokens[2], KEY);
                        Vars.CURRENT_LIBRARIAN_NAME = decryptString(tokens[3], KEY);
                        Vars.CURRENT_S_KEY = decryptInt(Integer.parseInt(tokens[4]), KEY);
                        Vars.CURRENT_L_KEY = decryptInt(Integer.parseInt(tokens[5]), KEY);
                        Vars.CURRENT_VIOLATION = decryptInt(Integer.parseInt(tokens[6]), KEY);
                        
                        //-------------------display temporarily-------------------
                        System.out.println("---------DECRYPTED---------");
                        System.out.println("curr-studentID: " + Vars.CURRENT_STUDENT_ID);
                        System.out.println("curr-librarianID: " + Vars.CURRENT_LIBRARIAN_ID);
                        System.out.println("curr-studentName: " + Vars.CURRENT_STUDENT_NAME);
                        System.out.println("curr-librarianName: " + Vars.CURRENT_LIBRARIAN_NAME);
                        System.out.println("curr-skey: " + Vars.CURRENT_S_KEY);
                        System.out.println("curr-lkey: " + Vars.CURRENT_L_KEY);
                        System.out.println("curr-violation: " + Vars.CURRENT_VIOLATION);
                        System.out.println("---------DECRYPTED---------");
                    }
                }
            }catch (FileNotFoundException e){
                System.out.println("ERROR: " + e.getMessage());
            }catch (IOException e){
                System.out.println("ERROR: " + e.getMessage());
            }
        }
        pause();
    }

    public void retrieveBooks() {
        //retrieve books from Database
        File fp = new File(Vars.LMS_BOOKS);
        if(!fp.exists()){
            if(Vars.CURRENT_S_KEY==1 && Vars.CURRENT_L_KEY==0){
                System.out.println("LIBRARY IS CURRENTLY EMPTY");
                System.out.println("NO BOOKS TO BE FOUND :(");
                System.out.println("PLEASE COME BACK LATER. SORRY FOR INCONVENIENCE.");
                pause();
                //logs("OUT", Vars.FILL);
                //exit(0);
            }else if(Vars.CURRENT_S_KEY==0 && Vars.CURRENT_L_KEY==1){
                System.out.println("LIBRARY IS CURRENTLY EMPTY");
                System.out.println("NO BOOKS TO BE FOUND :(");
                System.out.println("PLEASE ADD BOOKS.");
                pause();
            }else{
                System.out.println("SYSTEM ERROR.");
                pause();
            }
        }else{
            try (BufferedReader f2p = new BufferedReader(new FileReader(fp))){
                String str;

                while((str = f2p.readLine()) != null){
                    String[] tokens = str.split(",");

                    if (Vars.KEY_EXIST == false){
                        int bookNum = Integer.parseInt(tokens[0]);
                        String ISBN = tokens[1];
                        String bookTitle = tokens[2];
                        String bookAuthor = tokens[3];
                        int publicationYear = Integer.parseInt(tokens[4]);
                        int bookQuant = Integer.parseInt(tokens[5]);

                        //-------------------add the decrypted value to list-------------------
                        BookDetails book = new BookDetails(bookNum, ISBN, bookTitle, bookAuthor, publicationYear, bookQuant);
                        bookList.add(book);
                    }else {
                        //-------------------decrypting-------------------
                        int bookNum = decryptInt(Integer.parseInt(tokens[0]), KEY);
                        String ISBN = decryptString(tokens[1], KEY);
                        String bookTitle = decryptString(tokens[2], KEY);
                        String bookAuthor = decryptString(tokens[3], KEY);
                        int publicationYear = decryptInt(Integer.parseInt(tokens[4]), KEY);
                        int bookQuant = decryptInt(Integer.parseInt(tokens[5]), KEY);

                        //-------------------add the decrypted value to list-------------------
                        BookDetails book = new BookDetails(bookNum, ISBN, bookTitle, bookAuthor, publicationYear, bookQuant);
                        bookList.add(book);
                    }
                }
            }catch (FileNotFoundException e){
                System.out.println("ERROR: " + e.getMessage());
            }catch (IOException e){
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    public void registerStudent() {
        int count = 0;
        boolean isValid = false;

        while(!isValid){
            if(count<3){
                cls();

                System.out.printf("%nAPPLICATION FORM%n%n");
                System.out.print("\nEnter Student Name: ");
                String studentName = scan.nextLine();
                System.out.print("\nEnter Student ID: ");
                String studentID = scan.nextLine();

                if(checkAccount(1,studentID)==2){
                    int skey = Vars.CURRENT_S_KEY = 1;
                    int lkey = Vars.CURRENT_L_KEY = 0;
                    String librarianID = Vars.FILL;
                    String librarianName = Vars.FILL;
                    Vars.CURRENT_STUDENT_ID = studentID;
                    Vars.CURRENT_STUDENT_NAME = studentName;
                    int violation = Vars.CURRENT_VIOLATION = 0;

                    AccountDetails account = new AccountDetails(studentID, librarianID, studentName, librarianName, skey, lkey, violation);
                    accountList.add(account);
                    System.out.println("REGISTRATION SUCCESSFUL");
                    pause();
                    isValid = true;
                }else {
                    System.out.println("INVALID STUDENT ID");
                    System.out.println("ID ALREADY EXIST");
                    System.out.println("PLEASE TRY AGAIN.");
                    pause();
                    count++;
                }
            }else{
                System.out.println("ERROR.");
                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                System.out.println("PLEASE TRY AGAIN LATER");
                pause();
                System.exit(0);
            }
        }
        cls();
    }

    public void registerLibrarian() {
        int count = 0;
        boolean isValid = false;
        while(!isValid){
            if(count<3){
                cls();

                System.out.printf("%nAPPLICATION FORM%n%n");
                System.out.println("ADMINISTRATORS HAVE DEFAULT NAME AND ID");
                System.out.println("PLEASE ENTER THE FOLLOWING INFORMATION TO CONFIRM");
                System.out.println("Enter Librarian Name: ");
                String librarianName = scan.nextLine();
                System.out.println("Enter Librarian ID: ");
                String librarianID = scan.nextLine();

                if(librarianName.compareTo(Vars.ADMIN_FILL_NAME)==0 &&
                    librarianID.compareTo(Vars.ADMIN_FILL_ID)==0){

                    int skey = Vars.CURRENT_S_KEY = 0;
                    int lkey = Vars.CURRENT_L_KEY = 1;
                    String studentID = Vars.FILL;
                    String studentName = Vars.FILL;
                    Vars.CURRENT_LIBRARIAN_NAME = librarianName;
                    Vars.CURRENT_LIBRARIAN_ID = librarianID;
                    int violation = Vars.CURRENT_VIOLATION = 0;

                    AccountDetails account = new AccountDetails(studentID, librarianID, studentName, librarianName, skey, lkey, violation);

                    if (checkAccount(2,librarianID) == 2){
                        accountList.add(account);
                    }

                    System.out.println("REGISTRATION SUCCESSFUL");
                    pause();
                    isValid = true;
                }else{
                    System.out.println("INVALID INPUT");
                    System.out.println("CREDENTIALS DO NOT MATCH");
                    System.out.println("PLEASE TRY AGAIN.");
                    pause();
                    count++;
                }
            }else{
                System.out.println("ERROR.");
                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                System.out.println("PLEASE TRY AGAIN LATER");
                pause();
                System.exit(0);
            }
        }
        cls();
    }

    public static void userLOGIN() {
        int count;
        boolean isValid = false;
        /*Console cons = System.console();
        if (cons == null) {
            System.out.println("CONSOLE NOT AVAILABLE. EXITING...");
            pause();
            cls();
            //return;
        }*/

        if(Vars.CURRENT_S_KEY==1 && Vars.CURRENT_L_KEY==0){
            count = 0;
            while(!isValid){
                if(count<3){
                    cls();
                    System.out.printf("%nLOG IN%n%n");
                    //System.out.println("NOTE: INPUTS ARE HIDDEN FOR SECURITY PURPOSES.\n");

                    //hiding user input for security purposes
                    //char[] user_studentID = cons.readPassword("ENTER STUDENT ID: ");
                    //String studentID = new String(user_studentID);

                    System.out.print("ENTER STUDENT ID: ");
                    String studentID = scan.nextLine();
                    if(studentID.compareTo(Vars.CURRENT_STUDENT_ID) == 0){
                        logs("IN",Vars.FILL);
                        isValid = true;
                    }else{
                        System.out.printf("%n%nERROR.%n");
                        System.out.println("WRONG STUDENT ID");
                        System.out.println("PLEASE TRY AGAIN.");
                        pause();
                        count++;
                    }
                }else{
                    System.out.println("ERROR.");
                    System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                    System.out.println("PLEASE TRY AGAIN LATER");
                    pause();
                    System.exit(0);
                }
            }
        }else if(Vars.CURRENT_S_KEY==0 && Vars.CURRENT_L_KEY==1){
            count = 0;
            while(!isValid){
                if(count<3){
                    cls();
                    System.out.printf("%nLOG IN%n%n");
                    //System.out.println("NOTE: INPUTS ARE HIDDEN FOR SECURITY PURPOSES.\n");

                    //hiding user input for security purposes
                    //char[] user_librarianID = cons.readPassword("ENTER LIBRARIAN ID: ");
                    //String librarianID = new String(user_librarianID);
                    
                    System.out.print("ENTER LIBRARIAN ID: ");
                    String librarianID = scan.nextLine();
                    if(librarianID.compareTo(Vars.ADMIN_FILL_ID) == 0){
                        logs("IN",Vars.FILL);
                        isValid = true;
                    }else{
                        System.out.printf("%n%nERROR.%n");
                        System.out.println("WRONG LIBRARIAN ID");
                        System.out.println("PLEASE TRY AGAIN.");
                        pause();
                        count++;
                    }
                }else{
                    System.out.println("ERROR.");
                    System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                    System.out.println("PLEASE TRY AGAIN LATER");
                    pause();
                    System.exit(0);
                }
            }
        }else{
            cls();
            System.out.println("SYSTEM ERROR.");
            pause();
            cls();
        }
    }



    //----------------------[STUDENT methods]----------------------
    public static void borrowBook() {
        int count, userNum;
        boolean isValid = false;
        boolean isFound = false;

        int userBookNum = prompts(1);

        if (Vars.CURRENT_VIOLATION == 0){
            //search the book
            for (BookDetails book : bookList){
                if (book.getBookNum() == userBookNum){
                    isFound = true;

                    //get current date and time
                    LocalDate currentDate = LocalDate.now();
                    int currYear = currentDate.getYear();
                    int currMonth = currentDate.getMonthValue();
                    int currDay = currentDate.getDayOfMonth();

                    displayCurrentBook(userBookNum);

                    if (verifyAccount(1)){
                        String temp = book.getBookTitle();
                        count = 0;
                        while (!isValid){
                            if (count<3){
                                System.out.println("BOOK COPIES TO BE BORROWED: ");
                                try {
                                    userNum = scan.nextInt();
                                    scan.nextLine(); // Consume the newline character
                                    if (userNum > 0 && userNum < book.bookQuantity){
                                        isValid = true;
                                        int newBookQuant = book.getBookQuantity() - userNum;
                                        book.setBookQuantity(newBookQuant);

                                        //save the borrowed book to the flashdrive
                                        File fp = new File(Vars.LMS_FD_BORROWED_BOOKS);
                                        do{
                                            try {
                                                fp.createNewFile();
                                            }catch (IOException e){
                                                System.out.println("ERROR: " + e.getMessage());
                                            }
                                        }while(!fp.exists());

                                        //-------------------encrypting-------------------
                                        int encryptedcurrYear = encryptInt(currYear, KEY);
                                        int encryptedcurrMonth = encryptInt(currMonth, KEY);
                                        int encryptedcurrDay = encryptInt(currDay, KEY);
                                        int encryptedBookNum = encryptInt(book.getBookNum(), KEY);
                                        String encryptedISBN = encryptString(book.getISBN(), KEY);
                                        String encryptedBookTitle = encryptString(book.getBookTitle(), KEY);
                                        String encryptedBookAuthor = encryptString(book.getBookAuthor(), KEY);
                                        int encryptedPubYear = encryptInt(book.getPublicationYear(), KEY);
                                        int encryptedBookQuant = encryptInt(userNum, KEY);

                                        //-------------------saving-------------------
                                        try (BufferedWriter fprint = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
                                            String borrowedBookDetails = String.format("%d,%d,%d,%s,%s,%s,%s,%d,%d%n",
                                                        encryptedcurrYear, encryptedcurrMonth, encryptedcurrDay, encryptedBookNum, 
                                                        encryptedISBN, encryptedBookTitle, encryptedBookAuthor, encryptedPubYear, encryptedBookQuant);
                                            fprint.write(borrowedBookDetails);
                                            System.out.println("THE BOOK ( " + temp + " ) SUCCESSFULLY BORROWED.");

                                            saveBooks();
                                            logs("BORROWED BOOK", temp);
                                            pause();
                                            cls();
                                        }catch (IOException e){
                                            System.out.println("ERROR: " + e.getMessage());
                                        }
                                    }else{
                                        System.out.println("INVALID INPUT");
                                        System.out.println("PLEASE SPARE AT LEAST 1 COPY OF THE BOOK :(");
                                        System.out.println("PLEASE TRY AGAIN");
                                        cls();
                                    }
                                }catch (InputMismatchException e) {
                                    System.out.println("INVALID INPUT.");
                                    System.out.println("PLEASE ENTER A VALID NUMBER.");
                                    scan.nextLine(); // Clear the input buffer
                                    pause();
                                    count++;
                                }
                            }else{
                                System.out.println("ERROR.");
                                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                System.out.println("PLEASE TRY AGAIN LATER");
                                pause();
                                isValid = true;
                            }
                        }
                    }else{
                        System.out.println("TRANSACTION CANCELLED.");
                        System.out.println("INPUT DO NOT MATCH TO YOUR CREDENTIALS.");
                        System.out.println("PLEASE TRY AGAIN LATER");
                        pause();
                    }
                    break;
                }
            }
            if (!isFound){
                System.out.println("BOOK NUMBER: ( " + userBookNum + " ) NOT FOUND.");
                pause();
            }
        }else {
            System.out.println("TRANSACTION CANCELLED.");
            System.out.println("YOU HAVE A VIOLATION");
            System.out.println("PLEASE SETTLE YOUR VIOLATION FIRST AND TRY AGAIN LATER.");
            pause();
        }
    }

    public void returnBook() {
        boolean isFound = false;
        boolean isAccFound = false;

        cls();

        if (verifyAccount(1)){
            File fp = new File(Vars.LMS_FD_BORROWED_BOOKS);
            if (fp.exists()){
                //get current time
                LocalDate currentDate = LocalDate.now();
                int currYear = currentDate.getYear();
                int currMonth = currentDate.getMonthValue();
                int currDay = currentDate.getDayOfMonth();

                //read the file
                try (BufferedReader f2p = new BufferedReader(new FileReader(fp))){
                    String str;

                    while ((str = f2p.readLine()) != null){
                        String[] tokens = str.split(",");
                        //-------------------decrypting-------------------
                        int borrowedYear = decryptInt(Integer.parseInt(tokens[0]), KEY);
                        int borrowedMonth = decryptInt(Integer.parseInt(tokens[1]), KEY);
                        int borrowedDay = decryptInt(Integer.parseInt(tokens[2]), KEY);
                        int borrowedBookNum = decryptInt(Integer.parseInt(tokens[3]), KEY);
                        String borrowedISBN = decryptString(tokens[4], KEY);
                        String borrowedBookTitle = decryptString(tokens[5], KEY);
                        String borrowedBookAuthor = decryptString(tokens[6], KEY);
                        int borrowedPubYear = decryptInt(Integer.parseInt(tokens[7]), KEY);
                        int borrowedBookQuantity = decryptInt(Integer.parseInt(tokens[8]), KEY);

                        //check penalty
                        for (AccountDetails account : accountList){
                            if (account.getStudentID().compareTo(Vars.CURRENT_STUDENT_ID) == 0){
                                isAccFound = true;
                                if (currYear > borrowedYear){
                                    //penalty here
                                    int kept = currYear - borrowedYear;
                                    System.out.println("YOU KEPT THE BOOK FOR ( " + kept + " ) YEAR(S)!? WTF?");
                                    System.out.println("CONGRATS!");
                                    System.out.println("YOU HAVE A VIOLATION OF 3 OR KUNG ANO MAN");
                                    account.setViolation(3);
                                } else if (currMonth > borrowedMonth) {
                                    //penalty here
                                    int kept = currMonth - borrowedMonth;
                                    System.out.println("YOU KEPT THE BOOK FOR ( " + kept + " ) MONTH(S)!? WTF?");
                                    System.out.println("CONGRATS!");
                                    System.out.println("YOU HAVE A VIOLATION OF 2 OR KUNG ANO MAN");
                                    account.setViolation(2);
                                }else if ((currDay - borrowedDay) >= 3){
                                    //penalty here
                                    int kept = currDay - borrowedDay;
                                    System.out.println("YOU KEPT THE BOOK FOR ( " + kept + " ) DAY(S)!? WTF?");
                                    System.out.println("CONGRATS!");
                                    System.out.println("YOU HAVE A VIOLATION OF 1 OR KUNG ANO MAN");
                                    account.setViolation(1);
                                }else{
                                    //no penalty
                                    int kept = currDay - borrowedDay;
                                    System.out.println("YOU KEPT THE BOOK FOR ( " + kept + " ) DAY(S).");
                                    System.out.println("CONGRATS!");
                                    System.out.println("YOU DON'T HAVE A VIOLATION OR KUNG ANO MAN");
                                    account.setViolation(0);
                                }
                                break;
                            }
                        }
                        if (isAccFound){
                            saveAccounts();
                            saveAccountFD();
                        }else {
                            System.out.println("SYSTEM ERROR");
                            System.out.println("ACCOUNT NOT FOUND IN LIST");
                            pause();
                            cls();
                        }

                        //return the book to the database
                        for (BookDetails book : bookList){
                            if (book.getBookNum() == borrowedBookNum){
                                isFound = true;
                                int newBookQuant = book.getBookQuantity() + borrowedBookQuantity;
                                book.setBookQuantity(newBookQuant);
                                System.out.println("BOOK ( " + borrowedBookTitle + " ) SUCCESSFULLY RETURNED.");
                                saveBooks();
                                logs("RETURNED BOOK", borrowedBookTitle);
                                pause();
                                cls();
                                break;
                            }
                        }
                        if (!isFound){
                            BookDetails books = new BookDetails(borrowedBookNum, borrowedISBN, borrowedBookTitle,
                                    borrowedBookAuthor, borrowedPubYear, borrowedBookQuantity);
                            bookList.add(books);
                            System.out.println("BOOK ( " + borrowedBookTitle + " ) SUCCESSFULLY RETURNED.");
                            saveBooks();
                            logs("RETURNED BOOK", borrowedBookTitle);
                            pause();
                            cls();
                        }
                    }
                    saveBooks();
                }catch (FileNotFoundException e){
                    System.out.println("ERROR: " + e.getMessage());
                }catch (IOException e){
                    System.out.println("ERROR: " + e.getMessage());
                }
                
                //remove the borrowedBooks.txt
                try {
                    Path path = Paths.get(Vars.LMS_FD_BORROWED_BOOKS);
                    Files.delete(path);
                }catch (IOException e){
                    System.out.println("ERROR: " + e.getMessage());
                } 
            }else {
                System.out.println("ERROR.");
                System.out.println("NO BOOKS TO BE RETURN.");
                pause();
            }
        }else{
            System.out.println("TRANSACTION CANCELLED.");
            System.out.println("INPUT DO NOT MATCH TO YOUR CREDENTIALS.");
            System.out.println("PLEASE TRY AGAIN LATER");
            pause();
        }
    }

    public void requestBook() {
        cls();
        int count = 0, count2 = 0;
        String validRandomISBN = "";
        boolean isValid = false;
        boolean isCorrect = false;
        boolean isCancelled = false;

        if (Vars.CURRENT_VIOLATION == 0){
            while(!isValid){
                if (count<3){
                    cls();
    
                    int bookNum = getBookNumber();
                    System.out.println("BOOK ID: " + bookNum);
    
                    System.out.println("INPUT BOOK TITLE: ");
                    String bookTitle = scan.nextLine();
    
                    System.out.println("INPUT BOOK AUTHOR: ");
                    String bookAuthor = scan.nextLine();
    
                    System.out.println("INPUT PUBLICATION YEAR: ");
                    int pubYear = scan.nextInt();
                    scan.nextLine(); // Consume the newline character
    
                    System.out.println("INPUT BOOK QUANTITY: ");
                    int bookQuantity = scan.nextInt();
                    scan.nextLine(); // Consume the newline character
    
                    int userNum = prompts(4);
                    switch (userNum){
                        case 1: //student know the books' isbn
                                if (pubYear > 0 && pubYear <= 2006 && bookQuantity > 0){
                                    System.out.println("\n\nENTER VALID 10-DIGIT ISBN: ");
                                    String userISBN = scan.nextLine();
                                    int len = userISBN.length();
                                    if (len == 10){
                                        if (checkISBN(userISBN,10)){
                                            validRandomISBN = userISBN;
                                            isValid = true;
                                            isCancelled = false;
                                            break;
                                        }else {
                                            System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                            System.out.println("INVALID ISBN.");
                                            pause();
                                            cls();
                                            count++;
                                        }
                                    }else {
                                        System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                        System.out.println("INVALID ISBN.");
                                        pause();
                                        cls();
                                        count++;
                                    }
                                }else if (pubYear > 0 && pubYear > 2006 && bookQuantity > 0){
                                    System.out.println("\n\nENTER VALID 13-DIGIT ISBN: ");
                                    String userISBN = scan.nextLine();
                                    int len = userISBN.length();
                                    if (len == 13){
                                        if (checkISBN(userISBN,13)){
                                            validRandomISBN = userISBN;
                                            isValid = true;
                                            isCancelled = false;
                                            break;
                                        }else {
                                            System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                            System.out.println("INVALID ISBN.");
                                            pause();
                                            cls();
                                            count++;
                                        }
                                    }else {
                                        System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                        System.out.println("INVALID ISBN.");
                                        pause();
                                        cls();
                                        count++;
                                    }
                                }else {
                                    System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                    System.out.println("INVALID INPUT.");
                                    pause();
                                    cls();
                                    count++;
                                }
                                break;
                        case 2: //student dont know the books' isbn
                                //will generate valid isbn base on pub year
                                if (pubYear > 0 && pubYear <= 2006 && bookQuantity > 0){
                                    validRandomISBN = getISBN(10);
                                    isValid = true;
                                    isCancelled = false;
                                }else if (pubYear > 0 && pubYear > 2006 && bookQuantity > 0){
                                    validRandomISBN = getISBN(13);
                                    isValid = true;
                                    isCancelled = false;
                                }else{
                                    System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                    System.out.println("INVALID INPUT.");
                                    pause();
                                    cls();
                                    count++;
                                }
                                break;
                        case 3: //cancelled
                                isValid = true;
                                isCancelled = true;
                                break;
                        default: System.out.println("WRONG RETURN VALUE");
                                 pause();
                                 cls();
                                 break;
                    }
    
                    if (isValid && !isCancelled){
                        if (checkBooks(1, bookTitle, bookAuthor, 0)){
                            while (!isCorrect){
                                if (count2<3){
                                    if (verifyAccount(1)){
                                        isCorrect = true;
                                        //----------------encryption----------------
                                        int encryptedBookNum = encryptInt(bookNum, KEY);
                                        String encryptedISBN = encryptString(validRandomISBN, KEY);
                                        String encryptedBookTitle = encryptString(bookTitle, KEY);
                                        String encryptedBookAuthor = encryptString(bookAuthor, KEY);
                                        int encryptedPubYear = encryptInt(pubYear, KEY);
                                        int encryptedBookQuant = encryptInt(bookQuantity, KEY);

                                        //----------------saving----------------
                                        BookDetails book = new BookDetails(encryptedBookNum, encryptedISBN, encryptedBookTitle, 
                                                encryptedBookAuthor, encryptedPubYear, encryptedBookQuant);
                                        bookRquestList.add(book);
                                        saveBookRequests();
                                        logs("REQUEST BOOK", bookTitle);
    
                                        System.out.println("\nBOOK ( " + bookTitle + " ) SUCCESSFULLY REQUESTED.");
                                        pause();
                                        cls();
                                    }else {
                                        System.out.println("ERROR.");
                                        System.out.println("INPUT DO NOT MATCH TO YOU CREDENTIALS.");
                                        System.out.println("PLEASE TRY AGAIN");
                                        pause();
                                        cls();
                                        count2++;
                                    }
                                }else{
                                    System.out.println("ERROR.");
                                    System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                    System.out.println("PLEASE TRY AGAIN LATER");
                                    pause();
                                    cls();
                                    isCorrect = true;
                                }
                            }
                        }else {
                            userNum = prompts(5);
                            if (userNum == 1){
                                while (!isCorrect){
                                    if (count2<3){
                                        if (verifyAccount(2)){
                                            count = 0;
                                            count2 = 0;
                                            isCorrect = true;
                                            isValid = false;
                                        }else {
                                            System.out.println("ERROR.");
                                            System.out.println("INPUT DO NOT MATCH TO YOU CREDENTIALS.");
                                            System.out.println("PLEASE TRY AGAIN");
                                            pause();
                                            cls();
                                            count2++;
                                        }
                                    }else{
                                        System.out.println("ERROR.");
                                        System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                        System.out.println("GRABE KA NA");
                                        System.out.println("PLEASE TRY AGAIN LATER");
                                        pause();
                                        cls();
                                        isCorrect = true;
                                    }
                                }
                            }else {
                                System.out.println("PLEASE TRY AGAIN LATER");
                                pause();
                                cls();
                                isCorrect = true;
                            }
                        }
                    } else if (isValid && isCancelled) {
                            System.out.println("\nMODIFICATION CANCELLED.");
                            pause();
                            cls();
                    }
                }else {
                    System.out.println("ERROR.");
                    System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                    System.out.println("PLEASE TRY AGAIN LATER");
                    pause();
                    cls();
                    isValid = true;
                }
            }
        }else {
            System.out.println("TRANSACTION CANCELLED.");
            System.out.println("YOU HAVE A VIOLATION");
            System.out.println("PLEASE SETTLE YOUR VIOLATION FIRST AND TRY AGAIN LATER.");
            pause();
            cls();
        }
    }

    public static void viewViolation() {
        cls();
        boolean isFound = false;
        //locate the account
        for (AccountDetails account : accountList){
            if (account.getStudentID().compareTo(Vars.CURRENT_STUDENT_ID) == 0){
                isFound = true;
                if (account.getViolation() != 0){
                    System.out.println("YOU HAVE VIOLATION OF: " + account.getViolation());
                    int userNum = prompts(8);
                    if (userNum == 1){
                        account.setViolation(0);
                        Vars.CURRENT_VIOLATION = 0;
                        saveAccounts();
                        saveAccountFD();
                        logs("SETTLED VIOLATION", Vars.FILL);
                        System.out.println("CONGRATULATION YOU SETTLED YOUR VIOLATION.");
                        pause();
                        cls();
                    }else {
                        System.out.println("BRUH WTF SETTLE YOUR VIOLATION.");
                        pause();
                        cls();
                    }
                }else {
                    System.out.println("CONGRATULATION YOU DON'T HAVE ANY VIOLATION.");
                    pause();
                    cls();
                }
            }
        }
        if (!isFound){
            System.out.println("SYSTEM ERROR");
            System.out.println("ACCOUNT NOT FOUND IN LIST");
            pause();
            cls();
        }
    }


    //----------------------[LIBRARIAN methods]----------------------

    public static void insertExistingBook() {
        int count = 0, count2 = 0;
        boolean isValid = false;
        boolean isNum = false;
        cls();
        displayBooks();

        while (!isValid){
            if (count<3){
                try {
                    System.out.println("ENTER BOOK NUMBER: ");
                    int userNum = scan.nextInt();
                    scan.nextLine(); // Consume the newline character
    
                    //check if book exist
                    for (BookDetails book : bookList){
                        if (book.getBookNum() == userNum){
                            isValid = true;

                            System.out.println("BOOK SELECTED: ");
                            displayCurrentBook(book.bookNum);

                            while (!isNum){
                                if (count2<3){
                                    System.out.printf("%n%nENTER BOOK COPIES TO BE INSERTED: ");
                                    try {
                                        userNum = scan.nextInt();
                                        scan.nextLine(); // Consume the newline character
                                        if (userNum > 0){
                                            if (verifyAccount(2)){
                                                isNum = true;
                                                int newBookQuant = book.getBookQuantity() - userNum;
                                                book.setBookQuantity(newBookQuant);
                                                System.out.printf("%nMODIFICATION SUCCESSFUL.%n%n");
                                                System.out.println("BOOK LIST:");
                                                displayBooks();
                                                System.out.println("\n\nBOOK SELECTED:");
                                                displayCurrentBook(book.bookNum);
                                                saveBooks();
                                                logs("ADD EXISTING BOOK", book.getBookTitle());
                                                pause();
                                                cls();
                                            }else {
                                                System.out.println("INVALID INPUT");
                                                System.out.println("CREDENTIALS DO NOT MATCH");
                                                System.out.println("PLEASE TRY AGAIN.");
                                                pause();
                                                cls();
                                                count2++;
                                            }
                                        }else {
                                            System.out.println("\n\nINVALID INPUT.");
                                            System.out.println("PLEASE TRY AGAIN");
                                            pause();
                                            cls();
                                            count2++;
                                        }
                                    }catch (InputMismatchException e){
                                        System.out.println("\nERROR: " + e.getMessage());
                                        pause();
                                        cls();
                                        count2++;
                                    }
                                }else {
                                    System.out.println("ERROR.");
                                    System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                    System.out.println("PLEASE TRY AGAIN LATER");
                                    pause();
                                    cls();
                                    isNum = true;
                                }
                            }

                            break;
                        }
                    }
                    if (!isValid){
                        System.out.println("ERROR");
                        System.out.println("WRONG BOOK NUMBER OR BOOK NUMBER DOES NOT EXIST");
                        System.out.println("PLEASE TRY AGAIN");
                        pause();
                        cls();
                        count++;
                    }
                }catch (InputMismatchException e){
                    System.out.println("\nERROR: " + e.getMessage());
                    pause();
                    cls();
                }
            }else {
                System.out.println("ERROR.");
                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                System.out.println("PLEASE TRY AGAIN LATER");
                pause();
                cls();
                isValid = true;
            }
        }
    }

    public  void insertNewBook() {
        cls();
        int count = 0, count2 = 0;
        String validRandomISBN = "";
        boolean isValid = false;
        boolean isCorrect = false;

        while(!isValid){
            if (count<3){
                cls();

                int bookNum = getBookNumber();
                System.out.println("BOOK ID: " + bookNum);

                System.out.println("INPUT BOOK TITLE: ");
                String bookTitle = scan.nextLine();

                System.out.println("INPUT BOOK AUTHOR: ");
                String bookAuthor = scan.nextLine();

                System.out.println("INPUT PUBLICATION YEAR: ");
                int pubYear = scan.nextInt();
                scan.nextLine(); // Consume the newline character

                System.out.println("INPUT BOOK QUANTITY: ");
                int bookQuantity = scan.nextInt();
                scan.nextLine(); // Consume the newline character

                scan.nextLine(); // Consume the newline character

                int userNum = prompts(4);
                switch (userNum){
                    case 1: //librarian know the books' isbn
                            if (pubYear > 0 && pubYear <= 2006 && bookQuantity > 0){
                                System.out.println("\n\nENTER VALID 10-DIGIT ISBN: ");
                                String userISBN = scan.nextLine();
                                int len = userISBN.length();
                                if (len == 10){
                                    if (checkISBN(userISBN,10)){
                                        validRandomISBN = userISBN;
                                        isValid = true;
                                        break;
                                    }else {
                                        System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                        System.out.println("INVALID ISBN.");
                                        pause();
                                        cls();
                                        count++;
                                    }
                                }else {
                                    System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                    System.out.println("INVALID ISBN.");
                                    pause();
                                    cls();
                                    count++;
                                }
                            }else if (pubYear > 0 && pubYear > 2006 && bookQuantity > 0){
                                System.out.println("\n\nENTER VALID 13-DIGIT ISBN: ");
                                String userISBN = scan.nextLine();
                                int len = userISBN.length();
                                if (len == 13){
                                    if (checkISBN(userISBN,13)){
                                        validRandomISBN = userISBN;
                                        isValid = true;
                                        break;
                                    }else {
                                        System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                        System.out.println("INVALID ISBN.");
                                        pause();
                                        cls();
                                        count++;
                                    }
                                }else {
                                    System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                    System.out.println("INVALID ISBN.");
                                    pause();
                                    cls();
                                    count++;
                                }
                            }else {
                                System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                System.out.println("INVALID PUBLICATION YEAR.");
                                pause();
                                cls();
                                count++;
                            }
                            break;
                    case 2: //librarian dont know the books' isbn
                            //will generate valid isbn base on pub year
                            if (pubYear > 0 && pubYear <= 2006 && bookQuantity > 0){
                                validRandomISBN = getISBN(10);
                                isValid = true;
                            }else if (pubYear > 0 && pubYear > 2006 && bookQuantity > 0){
                                validRandomISBN = getISBN(13);
                                isValid = true;
                            }else{
                                System.out.println("\nMODIFICATION UNSUCCESSFUL.");
                                System.out.println("INVALID PUBLICATION YEAR.");
                                pause();
                                cls();
                                count++;
                            }
                            break;
                    case 3: //cancelled
                            System.out.println("\nMODIFICATION CANCELLED.");
                            isValid = true;
                            pause();
                            cls();
                            break;
                    default: System.out.println("WRONG RETURN VALUE");
                             pause();
                             cls();
                             break;
                }

                if (isValid){
                    if (checkBooks(1, bookTitle, bookAuthor, 0)){
                        while (!isCorrect){
                            if (count2<3){
                                if (verifyAccount(2)){
                                    isCorrect = true;
                                    BookDetails book = new BookDetails(bookNum, validRandomISBN, bookTitle, bookAuthor, pubYear, bookQuantity);
                                    bookList.add(book);
                                    saveBooks();
                                    logs("ADDED NEW BOOK", bookTitle);

                                    System.out.println("\nBOOK ( " + bookTitle + " ) SUCCESSFULLY ADDED.");
                                    pause();
                                    cls();
                                }else {
                                    System.out.println("ERROR.");
                                    System.out.println("INPUT DO NOT MATCH TO YOU CREDENTIALS.");
                                    System.out.println("PLEASE TRY AGAIN");
                                    pause();
                                    cls();
                                    count2++;
                                }
                            }else{
                                System.out.println("ERROR.");
                                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                System.out.println("PLEASE TRY AGAIN LATER");
                                pause();
                                cls();
                                isCorrect = true;
                            }
                        }
                    }else {
                        userNum = prompts(5);
                        if (userNum == 1){
                            while (!isCorrect){
                                if (count2<3){
                                    if (verifyAccount(2)){
                                        count = 0;
                                        count2 = 0;
                                        isCorrect = true;
                                        isValid = false;
                                    }else {
                                        System.out.println("ERROR.");
                                        System.out.println("INPUT DO NOT MATCH TO YOU CREDENTIALS.");
                                        System.out.println("PLEASE TRY AGAIN");
                                        pause();
                                        cls();
                                        count2++;
                                    }
                                }else{
                                    System.out.println("ERROR.");
                                    System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                    System.out.println("GRABE KA NA");
                                    System.out.println("PLEASE TRY AGAIN LATER");
                                    pause();
                                    cls();
                                    isCorrect = true;
                                }
                            }
                        }else {
                            System.out.println("PLEASE TRY AGAIN LATER");
                            pause();
                            cls();
                            isCorrect = true;
                        }
                    }
                }
            }else {
                System.out.println("ERROR.");
                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                System.out.println("PLEASE TRY AGAIN LATER");
                pause();
                cls();
                isValid = true;
            }
        }
    }

    public static void editBooks() {
        cls();
        int ch = 0, count = 0;
        int newBookNum, newBookYear, newBookQuant;
        String newISBN, newBookTitle, newBookAuthor;
        boolean isCorrect = false;
        boolean isFound = false;

        while (!isCorrect){
            if (count<3){
                if (verifyAccount(2)){
                    cls();
                    isCorrect = true;

                    System.out.println("\n\nENTER BOOK NUMBER TO BE EDITED: ");
                    int userBookNum = scan.nextInt();
                    scan.nextLine(); // Consume the newline character

                    for (BookDetails book : bookList){
                        if (book.getBookNum() == userBookNum){
                            isFound = true;
                            while (ch != 6){
                                //system("cls");
                                System.out.println("\n\n\nBOOK NUMBER: " + book.bookNum);
                                System.out.println("ISBN: " + book.ISBN);
                                System.out.println("BOOK TITLE: " + book.bookTitle);
                                System.out.println("BOOK AUTHOR: " + book.bookAuthor);
                                System.out.println("BOOK PUBLICATION YEAR: " + book.publicationYear);
                                System.out.println("BOOK QUANTITY: " + book.bookQuantity);
                                ch = prompts(6);
                                switch (ch){
                                    case 1: System.out.println("INPUT NEW VALID BOOK NUMBER: ");
                                            newBookNum = scan.nextInt();
                                            scan.nextLine(); // Consume the newline character
                                            if (checkBooks(2, null, null, newBookNum)){
                                                book.setBookNum(newBookNum);
                                                saveBooks();
                                                logs("EDIT BOOK NUMER", book.getBookTitle());
                                            }else {
                                                System.out.println("ERROR.");
                                                System.out.println("BOOK NUMBER ALREADY EXIST.");
                                                pause();
                                                cls();
                                            }
                                            break;
                                    case 2: System.out.println("INPUT NEW VALID BOOK TITLE: ");
                                            newBookTitle = scan.nextLine();
                                            if (checkBooks(1, newBookTitle, book.getBookAuthor(), 0)){
                                                logs("EDIT BOOK TITLE", "FROM ( " + book.getBookTitle() + " ) TO ( " + newBookTitle + " )");
                                                book.setBookTitle(newBookTitle);
                                                saveBooks();
                                            }else {
                                                System.out.println("ERROR.");
                                                System.out.println("BOOK DETAILS ALREADY EXIST.");
                                                pause();
                                                cls();
                                            }
                                            break;
                                    case 3: System.out.println("INPUT NEW VALID BOOK AUTHOR: ");
                                            newBookAuthor = scan.nextLine();
                                            if (checkBooks(1, book.getBookTitle(), newBookAuthor, 0)){
                                                logs("EDIT BOOK AUTHOR", "FROM ( " + book.getBookAuthor() + " ) TO ( " + newBookAuthor + " )");
                                                book.setBookTitle(newBookAuthor);
                                                saveBooks();
                                            }else {
                                                System.out.println("ERROR.");
                                                System.out.println("BOOK DETAILS ALREADY EXIST.");
                                                pause();
                                                cls();
                                            }
                                            break;
                                    case 4: System.out.println("INPUT NEW VALID BOOK PUBLICATION YEAR: ");
                                            newBookYear = scan.nextInt();
                                            scan.nextLine(); // Consume the newline character
                                            LocalDate currDate = LocalDate.now();
                                            if (newBookYear > 0 && newBookYear < currDate.getYear()){
                                                logs("EDIT BOOK PUB.YEAR", "FROM ( " + book.getPublicationYear() + " ) TO ( " + newBookYear + " )");
                                                book.setBookPubYear(newBookYear);

                                                //update the ISBN base on the new pub date
                                                if (newBookYear <= 2006){
                                                    newISBN = getISBN(10);
                                                }else {
                                                    newISBN = getISBN(13);
                                                }
                                                logs("EDIT BOOK ISBN", "FROM ( " + book.getISBN() + " ) TO ( " + newISBN + " )");
                                                book.setBookISBN(newISBN);
                                                saveBooks();
                                                pause();
                                                cls();
                                            }else {
                                                System.out.println("ERROR.");
                                                System.out.println("INVALID BOOK PUBLICATION YEAR.");
                                                pause();
                                                cls();
                                            }
                                            break;
                                    case 5: System.out.println("INPUT NEW VALID BOOK QUANTITY: ");
                                            newBookQuant = scan.nextInt();
                                            scan.nextLine(); // Consume the newline character
                                            if (newBookQuant > 0){
                                                logs("EDIT BOOK QUANTITY", "FROM ( " + book.getBookQuantity() + " ) TO ( " + newBookQuant + " )");
                                                book.setBookQuantity(newBookQuant);
                                                saveBooks();
                                            }else {
                                                System.out.println("ERROR.");
                                                System.out.println("INVALID BOOK QUANTITY.");
                                                pause();
                                                cls();
                                            }
                                            break;
                                    case 6: System.out.println("\n\nBOOK SUCCESSFULLY EDITED.");
                                            pause();
                                            cls();
                                            break;
                                    default: System.out.println("\n\nSELECT 1 - 6 ONLY.");
                                            pause();
                                            cls();
                                            break;
                                }
                            } 
                            saveBooks();
                            logs("EDIT BOOK", book.getBookTitle());
                            break;
                        }
                    }
                    if (!isFound){
                        System.out.println("ERROR NOT FOUND.");
                        System.out.println("WRONG BOOK NUMBER OR IT DOES NOT EXIST");
                        System.out.println("PLEASE TRY AGAIN LATER");
                        pause();
                        cls();
                        isCorrect = false;
                        count++;
                    }
                }else{
                    System.out.println("INVALID INPUT");
                    System.out.println("DOES NOT MATCH TO YOUR CREDENTIALS");
                    System.out.println("PLEASE TRY AGAIN");
                    pause();
                    cls();
                    count++;
                }
            }else {
                System.out.println("ERROR.");
                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                System.out.println("PLEASE TRY AGAIN LATER");
                pause();
                cls();
                isCorrect = true;
            }
        }
    }

    public static void removeAcopy() {
        cls();
        int count = 0, count2 = 0;
        boolean isValid = false;
        boolean isCorrect = false;

        while (!isValid){
            if (count<3){
                cls();
                System.out.println("\n\nENTER BOOK NUMBER TO REMOVE A COPY: ");
                int userBookNum = scan.nextInt();
                scan.nextLine(); // Consume the newline character

                for(BookDetails book : bookList){
                    if (book.getBookNum() == userBookNum){
                        isValid = true;
                        while (!isCorrect){
                            if (count2<3){
                                System.out.println("\n\nBOOK SELECTED: ");
                                displayCurrentBook(userBookNum);

                                System.out.println("ENTER NUMBER OF COPIES TO REMOVE: ");
                                int userQuantity = scan.nextInt();
                                scan.nextLine(); // Consume the newline character
                                if (userQuantity > 0 && userQuantity < book.getBookQuantity()){
                                    if (verifyAccount(2)){
                                        isCorrect = true;
                                        int newQuant = book.getBookQuantity() - userQuantity;
                                        book.setBookQuantity(newQuant);
                                        displayBooks();
                                        System.out.println("\n\nMODIFICATION SUCCESSFUL.");
                                        displayCurrentBook(userBookNum);

                                        saveBooks();
                                        logs("REMOVE A COPY", book.getBookTitle());
                                        pause();
                                        cls();
                                    }else {
                                        System.out.println("INVALID INPUT");
                                        System.out.println("DOES NOT MATCH TO YOUR CREDENTIALS");
                                        System.out.println("PLEASE TRY AGAIN");
                                        pause();
                                        cls();
                                        count2++;
                                    }
                                }else{
                                    System.out.println("ERROR.");
                                    System.out.println("iNVALID QUANTITY.");
                                    pause();
                                    cls();
                                    count2++;
                                }
                            }
                        }
                        break;
                    }
                }
                if (!isValid){
                    System.out.println("ERROR NOT FOUND.");
                    System.out.println("WRONG BOOK NUMBER OR IT DOES NOT EXIST");
                    System.out.println("PLEASE TRY AGAIN LATER");
                    pause();
                    cls();
                    count++;
                }
            }else {
                System.out.println("ERROR.");
                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                System.out.println("PLEASE TRY AGAIN LATER");
                pause();
                cls();
                isValid = true;
            }
        }
    }

    public static void removeBooks() {
        cls();
        int count = 0, count2 = 0;
        boolean isValid = false;
        boolean isCorrect = false;

        while (!isValid){
            if (count<3){
                cls();
                System.out.println("\n\nENTER BOOK NUMBER TO REMOVE: ");
                int userBookNum = scan.nextInt();
                scan.nextLine(); // Consume the newline character

                for(BookDetails book : bookList){
                    if (book.getBookNum() == userBookNum){
                        isValid = true;
                        while (!isCorrect){
                            if (count2<3){
                                System.out.println("\n\nBOOK SELECTED: ");
                                displayCurrentBook(userBookNum);

                                if (verifyAccount(2)){
                                    isCorrect = true;
                                    logs("REMOVED BOOK", book.getBookTitle());
                                    bookList.remove(book);
                                    displayBooks();
                                    System.out.println("\n\nMODIFICATION SUCCESSFUL.");

                                    saveBooks();
                                    pause();
                                    cls();
                                }else {
                                    System.out.println("INVALID INPUT");
                                    System.out.println("DOES NOT MATCH TO YOUR CREDENTIALS");
                                    System.out.println("PLEASE TRY AGAIN");
                                    pause();
                                    cls();
                                    count2++;
                                }
                            }
                        }
                        break;
                    }
                }
                if (!isValid){
                    System.out.println("ERROR NOT FOUND.");
                    System.out.println("WRONG BOOK NUMBER OR IT DOES NOT EXIST");
                    System.out.println("PLEASE TRY AGAIN LATER");
                    pause();
                    cls();
                    count++;
                }
            }else {
                System.out.println("ERROR.");
                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                System.out.println("PLEASE TRY AGAIN LATER");
                pause();
                cls();
                isValid = true;
            }
        }
    }

    public void bookRequests() {
        cls();
        int count = 0, count2 = 0;
        boolean isFound = false;
        boolean isCorrect = false;

        File fp = new File(Vars.LMS_FD_BORROWED_BOOKS);
        if (fp.exists()){
            try (BufferedReader f2p = new BufferedReader(new FileReader(fp))){
                cls();
                String str;
                
                while ((str = f2p.readLine()) != null){
                    String[] tokens = str.split(",");
                    //-------------------decryption-------------------
                    int reqBookNum = decryptInt(Integer.parseInt(tokens[0]), KEY);
                    String reqISBN = decryptString(tokens[1], KEY);
                    String reqBookTitle = decryptString(tokens[2], KEY);
                    String reqBookAuthor = decryptString(tokens[3], KEY);
                    int reqPublicationYear = decryptInt(Integer.parseInt(tokens[4]), KEY);
                    int reqBookQuant = decryptInt(Integer.parseInt(tokens[5]), KEY);

                    //-------------------add the decrypted value to list-------------------
                    BookDetails reqBook = new BookDetails(reqBookNum, reqISBN, reqBookTitle, reqBookAuthor, reqPublicationYear, reqBookQuant);
                    bookRquestList.add(reqBook);
                }

                //display contents
                System.out.println("\nBOOK REQUESTS");
                displayBookRequests();

                //approve or not
                int userNum = prompts(7);
                switch (userNum){
                    case 1: //approve book request
                        while (!isFound){
                            if (count<3){
                                cls();
                                System.out.println("ENTER BOOK NUMBER: ");
                                userNum = scan.nextInt();
                                scan.nextLine(); // Consume the newline character

                                //search book
                                for (BookDetails reqBook: bookRquestList){
                                    if (reqBook.getBookNum() == userNum){
                                        isFound = true;
                                        while (!isCorrect){
                                            if (count2<3){
                                                if (verifyAccount(2)){
                                                    isCorrect = true;
                                                    bookList.add(reqBook);
                                                    bookRquestList.remove(reqBook);
                                                    saveBooks();
                                                    saveBookRequests();
                                                    logs("APPROVED BOOK", reqBook.getBookTitle());

                                                    System.out.println("BOOK ( " + reqBook.getBookTitle() + " ) APPROVED.");
                                                    pause();
                                                    cls();
                                                }else {
                                                    System.out.println("INVALID INPUT");
                                                    System.out.println("DOES NOT MATCH TO YOUR CREDENTIALS");
                                                    System.out.println("PLEASE TRY AGAIN");
                                                    pause();
                                                    cls();
                                                    count2++;
                                                }
                                            }else {
                                                System.out.println("ERROR.");
                                                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                                System.out.println("PLEASE TRY AGAIN LATER");
                                                pause();
                                                cls();
                                                isCorrect = true;
                                            }
                                        }
                                        break;
                                    }
                                }
                                if (!isFound){
                                    System.out.println("ERROR");
                                    System.out.println("WRONG BOOK NUMBER OR BOOK NUMBER DOES NOT EXIST");
                                    System.out.println("PLEASE TRY AGAIN");
                                    pause();
                                    cls();
                                    count++;
                                }
                            }else {
                                System.out.println("ERROR.");
                                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                System.out.println("PLEASE TRY AGAIN LATER");
                                pause();
                                cls();
                                isFound = true;
                            }
                        }
                        break;
                    case 2: //disapprove book
                        while (!isFound){
                            if (count<3){
                                cls();
                                System.out.println("ENTER BOOK NUMBER: ");
                                userNum = scan.nextInt();
                                scan.nextLine(); // Consume the newline character

                                //search book
                                for (BookDetails reqBook: bookRquestList){
                                    if (reqBook.getBookNum() == userNum){
                                        isFound = true;
                                        while (!isCorrect){
                                            if (count2<3){
                                                if (verifyAccount(2)){
                                                    isCorrect = true;
                                                    bookRquestList.remove(reqBook);
                                                    saveBooks();
                                                    saveBookRequests();
                                                    logs("DISAPPROVED BOOK", reqBook.getBookTitle());

                                                    System.out.println("BOOK ( " + reqBook.getBookTitle() + " ) DISAPPROVED.");
                                                    pause();
                                                    cls();
                                                }else {
                                                    System.out.println("INVALID INPUT");
                                                    System.out.println("DOES NOT MATCH TO YOUR CREDENTIALS");
                                                    System.out.println("PLEASE TRY AGAIN");
                                                    pause();
                                                    cls();
                                                    count2++;
                                                }
                                            }else {
                                                System.out.println("ERROR.");
                                                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                                System.out.println("PLEASE TRY AGAIN LATER");
                                                pause();
                                                cls();
                                                isCorrect = true;
                                            }
                                        }
                                        break;
                                    }
                                }
                                if (!isFound){
                                    System.out.println("ERROR");
                                    System.out.println("WRONG BOOK NUMBER OR BOOK NUMBER DOES NOT EXIST");
                                    System.out.println("PLEASE TRY AGAIN");
                                    pause();
                                    cls();
                                    count++;
                                }
                            }else {
                                System.out.println("ERROR.");
                                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                                System.out.println("PLEASE TRY AGAIN LATER");
                                pause();
                                cls();
                                isFound = true;
                            }
                        }
                        break;
                    case 3: //save and exit
                            saveBooks();
                            saveBookRequests();
                        break;
                    default: System.out.println("INVALID INPUT.");
                            pause();
                            cls();
                            break;
                            
                }
            }catch (FileNotFoundException e){
                System.out.println("\nERROR: " + e.getMessage());
                pause();
                cls();
            }catch (IOException e){
                System.out.println("\nERROR: " + e.getMessage());
                pause();
                cls();
            }
        }else {
            System.out.println("ERROR.");
            System.out.println("NO BOOK REQUESTS YET.");
            pause();
            cls();
        }
    }

    public static void changeKey() {
        cls();
        int newKey = 0, count = 0;
        boolean isValid = false;
        Random srand = new Random();
        File fp = new File(Vars.LMS_KEY);

        if (verifyAccount(2)){
            int userNum = prompts(9);
            switch (userNum){
                case 1: //generate random valid key
                        do{
                            newKey = srand.nextInt(100);
                        }while (newKey < 1 && newKey != KEY);

                        //save the key to the file
                        if (!fp.exists()){
                            try {
                                fp.createNewFile();
                            }catch (IOException e){
                                System.out.println("ERROR: " + e.getMessage());
                                pause();
                                cls();
                            }
                        }

                        try (BufferedWriter fwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
                            fwrite.write(String.valueOf(newKey));
                        }catch (IOException e){
                            System.out.println("ERROR: " + e.getMessage());
                            pause();
                            cls();
                        }

                        KEY = newKey;
                        System.out.println("KEY IS SET TO: " + KEY);
                        pause();
                        cls();
                        break;
                case 2: //librarian will enter the new key
                        while (!isValid){
                            if (count<3){
                                System.out.println("ENTER VALID KEY: ");
                                try {
                                    newKey = scan.nextInt();
                                    if (newKey > 1 && newKey < 100 && newKey != KEY){
                                        isValid = true;
                                        //save the key to the file
                                        if (!fp.exists()){
                                            try {
                                                fp.createNewFile();
                                            }catch (IOException e){
                                                System.out.println("ERROR: " + e.getMessage());
                                                pause();
                                                cls();
                                            }
                                        }

                                        try (BufferedWriter fwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
                                            fwrite.write(String.valueOf(newKey));
                                        }catch (IOException e){
                                            System.out.println("ERROR: " + e.getMessage());
                                            pause();
                                            cls();
                                        }

                                    }else {
                                        System.out.println("PLEASE ENTER 'NEW' AND 'VALID' KEY.");
                                        System.out.println("PLEASE TRY AGAIN.");
                                        pause();
                                        cls();
                                        count++;
                                    }
                                }catch (InputMismatchException e){
                                    System.out.println("ERROR: " + e.getMessage());
                                    pause();
                                    cls();
                                    count++;
                                }
                            }
                        }
                        break;
                default: System.out.println("WRONG RETURN VALUE.");
                         System.out.println("PLEASE CHECK THE CODE.");
                         pause();
                         cls();
                         break;
                        
            }
        }else {
            System.out.println("INVALID INPUT");
            System.out.println("CREDENTIALS DO NOT MATCH");
            System.out.println("PLEASE TRY AGAIN LATER.");
            pause();
            cls();
        }

        //check if the files are existed
        //if existed save the data to the new key
        fp = new File(Vars.LMS_ACCOUNTS);
        if (fp.exists()) {
            saveAccounts();
        }
        fp = new File(Vars.LMS_FD_USER_DETAILS);
        if (fp.exists()) {
            saveAccountFD();
        } 
        fp = new File(Vars.LMS_BOOKS);
        if (fp.exists()) {
            saveBooks();
        }
        fp = new File(Vars.LMS_BOOK_REQUESTS);
        if (fp.exists()) {
            saveBookRequests();
        }
    }

    public static void displayLogs() {
        int count = 0, userNum;
        boolean isValid = false;
        boolean isValid2 = false;

        while(!isValid){
            if(count<3){
                cls();
                displayDirectoryContent();
                System.out.printf("%n%n!DO NOT INCLUDE THE .csv EXTENSION!%n");
                System.out.printf("%n%nENTER DATE (YYYY-MM-DD): ");
                String userStr = scan.nextLine();

                File fp = new File(Vars.LMS_LOGS + "\\" + userStr + ".csv");
                if(fp.exists()){
                    try (BufferedReader f2p = new BufferedReader(new FileReader(fp))) {
                        String str;

                        System.out.printf("%n| %-11s | %-11s | %-50s | %-10s | %-10s |%n",
                                "DATE", "TIME", "NAME", "STATUS", "ITEM");

                        while((str = f2p.readLine()) != null){
                            String[] tokens = str.split(",");

                            System.out.printf("| %-11s | %-11s | %-50s | %-10s | %-10s |%n",
                                    tokens[0], tokens[1], tokens[2], tokens[3], tokens[4]);
                        }
                        while (!isValid2){
                            System.out.printf("%n%n");
                            System.out.println("DO YOU WANT TO VIEW OTHER DATES? ");
                            System.out.println("[1] YES");
                            System.out.println("[2] NO");
                            System.out.println("-> ");
                            try{
                                userNum = scan.nextInt();
                                scan.nextLine(); // Consume the newline character
                                if (userNum > 0 && userNum <= 2){
                                    if (userNum == 1){
                                        isValid2 = true;
                                    }else{
                                        isValid = true;
                                        isValid2 = true;
                                    }
                                }else{
                                    System.out.println("INVALID INPUT");
                                    System.out.println("PLEASE TRY AGAIN.");
                                    pause();
                                    cls();
                                }
                            }catch (InputMismatchException e){
                                System.out.println("\nERROR: " + e.getMessage());
                                pause();
                                cls();
                            }
                        }
                    }catch (FileNotFoundException e){
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                        cls();
                    }catch (IOException e){
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                        cls();
                    }
                    isValid2 = false;
                    pause();
                }else{
                    System.out.println("ERROR.");
                    System.out.println("FILE ( " + userStr + ".csv ) NOT FOUND.");
                    System.out.println("PLEASE TRY AGAIN.");
                    pause();
                    cls();
                    count++;
                }
            }else{
                System.out.println("ERROR.");
                System.out.println("TOO MANY UNSUCCESSFUL ATTEMPTS.");
                System.out.println("PLEASE TRY AGAIN LATER");
                pause();
                cls();
                isValid = true;
            }
        }
        cls();
    }

    public static void displayDirectoryContent() {
        File directory = new File(Vars.LMS_LOGS);

        //Get the contents of the directory
        File[] files = directory.listFiles();

        //Display the contents of the directory
        if (files != null){
            for (File file : files){
                System.out.println(file.getName());
            }
        }
        System.out.printf("%n%n");
    }




    //----------------------[UTILITY]----------------------

    public static int menu(int x) {
        int userNum=0;
        boolean isValid = false;
        cls();
        while (!isValid){
            System.out.printf("%n%n%nMENU%n%n");

            if (x==1){ //student
                System.out.println("[1] DISPLAY BOOKS");
                System.out.println("[2] BORROW BOOKS");
                System.out.println("[3] RETURN BOOKS");
                System.out.println("[4] REQUEST BOOKS");
                System.out.println("[5] VIEW VIOLATIONS");
                System.out.println("[6] SAVE AND EXIT");
                System.out.printf("%n->");

                try {
                    userNum = scan.nextInt();
                    scan.nextLine(); // Consume the newline character
                    if (userNum > 0 && userNum <= 6){
                        isValid = true;
                    }else{
                        System.out.println("INVALID INPUT.");
                        System.out.println("PLEASE TRY AGAIN");
                        pause();
                        cls();
                    }
                }catch (InputMismatchException e) {
                    System.out.println("ERROR: " + e.getMessage());
                    pause();
                    cls();
                }
            }else{ //librarian
                System.out.println("[1] ADD BOOKS");
                System.out.println("[2] EDIT BOOKS");
                System.out.println("[3] REMOVE BOOKS");
                System.out.println("[4] BOOK REQUESTS");
                System.out.println("[5] CHANGE KEY");
                System.out.println("[6] DISPLAY BOOKS");
                System.out.println("[7] DISPLAY LOGS");
                System.out.println("[8] SAVE AND EXIT");
                System.out.printf("%n->");

                try {
                    userNum = scan.nextInt();
                    scan.nextLine(); // Consume the newline character
                    if (userNum > 0 && userNum <= 8){
                        isValid = true;
                    }else{
                        System.out.println("INVALID INPUT.");
                        System.out.println("PLEASE TRY AGAIN");
                        pause();
                        cls();
                    }
                }catch (InputMismatchException e) {
                    System.out.println("\nERROR: " + e.getMessage());
                    pause();
                    cls();
                }
            }
        }
        return userNum;
    }

    public static int prompts(int x) {
        int userNum = 0;
        boolean isValid = false;
        switch (x){
            case 1: // [borrowBooks]
                System.out.printf("%n%nINPUT BOOK NUMBER: ");
                userNum = scan.nextInt();
                scan.nextLine(); // Consume the newline character
                break;
            case 2: // main under case 1 [addBooks]
                while (!isValid){
                    System.out.printf("%n%nINSERT EXISTING BOOK?%n");
                    System.out.println("[1] YES");
                    System.out.println("[2] NO");
                    System.out.println("[3] CANCEL");
                    System.out.print("-> ");

                    try {
                        userNum = scan.nextInt();
                        scan.nextLine(); // Consume the newline character
                        if (userNum > 0 && userNum <= 3){
                            return userNum;
                        }else{
                            System.out.println("INVALID INPUT.");
                            System.out.println("PLEASE TRY AGAIN");
                            pause();
                        }
                    }catch (InputMismatchException e) {
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                    }
                }
                break;
            case 3: // main under case 3 [removeBooks]
                while (!isValid){
                    System.out.printf("%n%nREMOVE BOOK%n");
                    System.out.println("[1] REMOVE 'X' NUMBER OF COPY");
                    System.out.println("[2] REMOVE ALL BOOK COPIES");
                    System.out.println("[3] CANCEL");
                    System.out.print("-> ");

                    try {
                        userNum = scan.nextInt();
                        scan.nextLine(); // Consume the newline character
                        if (userNum > 0 && userNum <= 3){
                            return userNum;
                        }else{
                            System.out.println("INVALID INPUT.");
                            System.out.println("PLEASE TRY AGAIN");
                            pause();
                        }
                    }catch (InputMismatchException e) {
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                    }
                }
                break;
            case 4: //insertNewBook method
                while (!isValid){
                    System.out.println("DO YOU KNOW THE BOOKS' ISBN? ");
                    System.out.println("[1] YES");
                    System.out.println("[2] NO");
                    System.out.println("[3] CANCEL");
                    System.out.print("-> ");

                    try {
                        userNum = scan.nextInt();
                        scan.nextLine(); // Consume the newline character
                        if (userNum > 0 && userNum <= 3){
                            return userNum;
                        }else{
                            System.out.println("INVALID INPUT.");
                            System.out.println("PLEASE TRY AGAIN");
                            pause();
                        }
                    }catch (InputMismatchException e) {
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                    }
                }
                break;
            case 5: //insertNewBook method bandang huli
                while (!isValid){
                    System.out.println("BOOK ALREADY EXIST.");
                    System.out.println("DO YOU WANT TO TRY AGAIN?");
                    System.out.println("[1] YES");
                    System.out.println("[2] NO");
                    System.out.print("-> ");

                    try {
                        userNum = scan.nextInt();
                        scan.nextLine(); // Consume the newline character
                        if (userNum > 0 && userNum <= 2){
                            return userNum;
                        }else{
                            System.out.println("INVALID INPUT.");
                            System.out.println("PLEASE TRY AGAIN");
                            pause();
                        }
                    }catch (InputMismatchException e) {
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                    }
                }
                break;
            case 6: //editbooks menu/choices
                while (!isValid){
                    System.out.println("CHANGE BOOK DETAILS");
                    System.out.println("[1] BOOK NUMBER");
                    System.out.println("[2] BOOK TITLE");
                    System.out.println("[3] BOOK AUTHOR");
                    System.out.println("[4] BOOK PUBLICATION YEAR");
                    System.out.println("[5] BOOK QUANTITY");
                    System.out.println("[6] SAVE & EXIT");
                    System.out.print("-> ");

                    try {
                        userNum = scan.nextInt();
                        scan.nextLine(); // Consume the newline character
                        if (userNum > 0 && userNum <= 6){
                            return userNum;
                        }else{
                            System.out.println("INVALID INPUT.");
                            System.out.println("PLEASE TRY AGAIN");
                            pause();
                        }
                    }catch (InputMismatchException e) {
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                    }
                }
                break;
            case 7: //book request options
                while (!isValid){
                    System.out.println("OPTIONS");
                    System.out.println("[1] APPROVE BOOK REQUEST");
                    System.out.println("[2] DISAPPROVE BOOK REQUEST");
                    System.out.println("[3] SAVE & EXIT");
                    System.out.print("-> ");

                    try {
                        userNum = scan.nextInt();
                        scan.nextLine(); // Consume the newline character
                        if (userNum > 0 && userNum <= 3){
                            return userNum;
                        }else{
                            System.out.println("INVALID INPUT.");
                            System.out.println("PLEASE TRY AGAIN");
                            pause();
                        }
                    }catch (InputMismatchException e) {
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                    }
                }
                break;
            case 8: //VIEWvIOLATION METHOD
                while (!isValid){
                    System.out.println("DO YOU WANT TO SETTLE YOUR VIOLATION?");
                    System.out.println("[1] YES");
                    System.out.println("[2] NO");
                    System.out.print("-> ");

                    try {
                        userNum = scan.nextInt();
                        scan.nextLine(); // Consume the newline character
                        if (userNum > 0 && userNum <= 2){
                            return userNum;
                        }else{
                            System.out.println("INVALID INPUT.");
                            System.out.println("PLEASE TRY AGAIN");
                            pause();
                        }
                    }catch (InputMismatchException e) {
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                    }
                }
                break;
            case 9: //CHANGE KEY METHOD
                while (!isValid){
                    System.out.println("SELECT METHOD TO USE");
                    System.out.println("[1] GENERATE RANDOM VALID KEY");
                    System.out.println("[2] ENTER OWN VALID KEY");
                    System.out.print("-> ");

                    try {
                        userNum = scan.nextInt();
                        scan.nextLine(); // Consume the newline character
                        if (userNum > 0 && userNum <= 2){
                            return userNum;
                        }else{
                            System.out.println("INVALID INPUT.");
                            System.out.println("PLEASE TRY AGAIN");
                            pause();
                        }
                    }catch (InputMismatchException e) {
                        System.out.println("\nERROR: " + e.getMessage());
                        pause();
                    }
                }
                break;

            default: cls();
                System.out.printf("%n%nINVALID PARAMETER.%n");
                pause();
                cls();
                break;
        }
        return userNum;
    }

    public static void displayBooks() {
        cls();
        System.out.println("-------------------------------------------------------------");
        System.out.printf("| %-8s | %-15s | %-74s | %-82s | %-9s | %-8s |%n",
                "BOOK NUM", "ISBN", "BOOK TITLE", "BOOK AUTHOR", "PUB. YEAR", "QUANTITY");
        System.out.println("-------------------------------------------------------------");

        for (BookDetails book : bookList){
            System.out.printf("| %-8s | %-15s | %-74s | %-82s | %-9s | %-8s |%n",
                    book.getBookNum(), book.getISBN(), book.getBookTitle(),
                    book.getBookAuthor(), book.getPublicationYear(), book.getBookQuantity());
        }
        System.out.println("-------------------------------------------------------------");
        System.out.printf("%n%n");
    }

    public static void displayCurrentBook(int x){
        System.out.printf("%n-------------------------------------------------------------");
        System.out.printf("%n%nBook Details:%n");
        System.out.printf("| %-8s | %-15s | %-74s | %-82s | %-9s | %-8s |%n",
                "BOOK NUM", "ISBN", "BOOK TITLE", "BOOK AUTHOR", "PUB. YEAR", "QUANTITY");

        //search for the book
        for (BookDetails book : bookList){
            if (book.getBookNum() == x){
                System.out.printf("| %-8d | %-15s | %-74s | %-82s | %-9d | %-8d |%n",
                        book.getBookNum(), book.getISBN(), book.getBookTitle(),
                        book.getBookAuthor(), book.getPublicationYear(), book.getBookQuantity());
            }
        }
    }

    public static void displayBookRequests() {
        cls();
        System.out.println("-------------------------------------------------------------");
        System.out.printf("| %-8s | %-15s | %-74s | %-82s | %-9s | %-8s |%n",
                "BOOK NUM", "ISBN", "BOOK TITLE", "BOOK AUTHOR", "PUB. YEAR", "QUANTITY");
        System.out.println("-------------------------------------------------------------");

        for (BookDetails book : bookRquestList){
            System.out.printf("| %-8s | %-15s | %-74s | %-82s | %-9s | %-8s |%n",
                    book.getBookNum(), book.getISBN(), book.getBookTitle(),
                    book.getBookAuthor(), book.getPublicationYear(), book.getBookQuantity());
        }
        System.out.println("-------------------------------------------------------------");
        System.out.printf("%n%n");
    }

    public static int checkAccount(int x, String ID) {
        boolean isFound = false;
        if (x == 1){ 
            //-----------------student-----------------
            for (AccountDetails account : accountList){
                if (account.getStudentID().compareTo(ID) == 0){
                    return 1;
                }
            }
            if (!isFound){
                return 2;
            }
        }else { 
            //-----------------librarian-----------------
            for (AccountDetails account: accountList){
                if (account.getLibrarianID().compareTo(ID) == 0){
                    return 1;
                }else{
                    return 2;
                }
            }
        }
        return 0;
    }

    public static boolean verifyAccount(int x) {
        /*Console cons = System.console();
        if (cons == null) {
            System.out.println("CONSOLE NOT AVAILABLE. EXITING...");
            pause();
            cls();
            return false;
        }*/
        System.out.println("NOTE: INPUTS ARE HIDDEN FOR SECURITY PURPOSES.\n");

        if (x==1){ 
            //-----------------student-----------------
            //hiding user input for security purposes
            //char[] user_studentID = cons.readPassword("ENTER STUDENT ID TO CONFIRM: ");
            //String userStr = new String(user_studentID);
            
            System.out.print("ENTER STUDENT ID: ");
            String userStr = scan.nextLine();
            return userStr.compareTo(Vars.CURRENT_STUDENT_ID) == 0;
        }else{ 
            //-----------------librarian-----------------
            //char[] user_librarianID = cons.readPassword("ENTER LIBRARIAN ID TO CONFIRM: ");
            //String userStr = new String(user_librarianID);

            System.out.print("ENTER LIBRARIAN ID: ");
            String userStr = scan.nextLine();
            return userStr.compareTo(Vars.ADMIN_FILL_ID) == 0;
        }
    }

    public static int getBookNumber() {
        int newBookNum = 0;
        boolean isValid = false;
        Random srand = new Random();
        while (!isValid){
            do{
                newBookNum = srand.nextInt();
            }while (newBookNum < 0);

            for (BookDetails book : bookList){
                if (book.getBookNum() != newBookNum){
                    isValid = true;
                    break;
                }
            }
        }
        return newBookNum;
    }

    public static String getISBN(int x) {
        long randomISBN;
        String randomISBNstring = "";
        boolean isValid = false;
        Random srand = new Random();
        switch (x) {
            case 10:
                while (!isValid){
                    do{
                        randomISBN = srand.nextInt(999999999) + 1000000000;
                    }while (randomISBN <= 0);
                    
                    randomISBNstring = String.valueOf(randomISBN);
                    int len = randomISBNstring.length();
                    
                    //check if valid
                    if (len == x){
                        if (checkISBN(randomISBNstring, x)){
                            isValid = true;
                        }
                    }
                }   break;
            case 13:
                while (!isValid){
                    do{
                        randomISBN = Math.abs(srand.nextLong() % 9000000000000L) + 1000000000000L;
                    }while (randomISBN <= 0);
                    
                    randomISBNstring = String.valueOf(randomISBN);
                    int len = randomISBNstring.length();
                    
                    //check if valid
                    if (len == x){
                        if (checkISBN(randomISBNstring, x)){
                            isValid = true;
                        }
                    }
                }   break;
            default:
                System.out.println("\n\nWRONGPARAMETER.");
                System.out.println("PLEASE CHECK THE CODE.");
                pause();
                cls();
                break;
        }
        
        return randomISBNstring;
    }

    public static boolean checkISBN(String str, int x) {
        int sum = 0;
        switch (x) {
            case 10:
            {
                for (int i=0; i<9; i++){
                    char c = str.charAt(i);
                    
                    if (!Character.isDigit(c)){
                        return false;
                    }
                    
                    int digit = Character.getNumericValue(c);
                    sum += (digit * (x - i));
                }
                
                //Check if last char is a digit or 'X'
                char checkLastChar = str.charAt(9);
                if (!Character.isDigit(checkLastChar) && checkLastChar != 'X'){
                    return false;
                }
                
                //else if last char is 'X'
                int lastDigit = (checkLastChar == 'X') ? 10 : Character.getNumericValue(checkLastChar);
                sum += lastDigit;
                
                //check if isbn is existing or not
                for (BookDetails book : bookList){
                    if (book.getISBN().compareTo(str) == 0){
                        return false;
                    }
                }
                
                //return true if sum is divisible by 11
                return sum % 11 == 0;
            }
            case 13:
            {
                for (int i=0; i<12; i++){
                    char c = str.charAt(i);
                    
                    if (!Character.isDigit(c)){
                        return false;
                    }
                    
                    //multiply the digit by 1 if the index is even else
                    //multiply the digit by 3 if the index is odd
                    //why? ewan ko ganon daw e HAHAHAHA
                    int digit = Character.getNumericValue(c);
                    sum += (i % 2 == 0) ? digit : digit * 3;
                }
                
                //Check if last char is a digit or 'X'
                char checkLastChar = str.charAt(12);
                if (!Character.isDigit(checkLastChar) && checkLastChar != 'X'){
                    return false;
                }
                
                //else if last char is 'X'
                int lastDigit = (checkLastChar == 'X') ? 10 : Character.getNumericValue(checkLastChar);
                sum += lastDigit;
                
                //check if isbn is existing or not
                for (BookDetails book : bookList){
                    if (book.getISBN().compareTo(str) == 0){
                        return false;
                    }
                }
                
                //return true if sum is divisible by 10
                //why 10? ewan ko hindi raw same sa 10 digit na divisble by 11 dapat
                return sum % 10 == 0;
            }
            default:
                System.out.println("\n\nWRONG PARAMETERS.");
                System.out.println("PLEASE CHECK THE CODE.");
                pause();
                cls();
                return false;
        }
    }

    public static boolean checkBooks(int x, String title, String author, int bNum) {
        switch (x) {
            case 1:
                for (BookDetails book : bookList){
                    if (book.getBookTitle().equalsIgnoreCase(title) && book.getBookAuthor().equalsIgnoreCase(author)){
                        displayCurrentBook(book.getBookNum());
                        return false;
                    }
                }   break;
            case 2:
                for (BookDetails book : bookList){
                    if (book.getBookNum() == bNum){
                        return false;
                    }
                }   break;
            default:
                System.out.println("\n\nWRONG PARAMETERS.");
                System.out.println("PLEASE CHECK THE CODE.");
                pause();
                cls();
                return false;
        }
        
        return true;
    }

    public static void getKey() {
        File fp = new File(Vars.LMS_KEY);
        Random srand = new Random();

        do{
            KEY = srand.nextInt(100);
        }while (KEY < 1);

        //save the key to the file
        if (!fp.exists()){
            try {
                fp.createNewFile();
            }catch (IOException e){
                System.out.println("ERROR: " + e.getMessage());
                pause();
                cls();
            }
        }

        try (BufferedWriter fwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
            fwrite.write(String.valueOf(KEY));
        }catch (IOException e){
            System.out.println("ERROR: " + e.getMessage());
            pause();
            cls();
        }
    }


    public static void saveAccounts() {
        File fp = new File(Vars.LMS_ACCOUNTS);
        if (!fp.exists()){
            try {
                fp.createNewFile();
            } catch (IOException e) {
                System.out.println("\nERROR: " + e.getMessage());
                pause();
                cls();
            }
        }

        try (BufferedWriter fwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
            for (AccountDetails account : accountList){
                //-------------------encrypting-------------------
                String encryptedStudentID = encryptString(account.getStudentID(), KEY);
                String encryptedLibrarianID = encryptString(account.getLibrarianID(), KEY);
                String encryptedStudentName = encryptString(account.getStudentName(), KEY);
                String encryptedLibrarianName = encryptString(account.getLibrarianName(), KEY);
                int encryptedSkey = encryptInt(account.getSkey(), KEY);
                int encryptedLkey = encryptInt(account.getLkey(), KEY);
                int encryptedViolation = encryptInt(account.getViolation(), KEY);

                //-------------------saving-------------------
                fwrite.write(encryptedStudentID + "," + encryptedLibrarianID + "," +
                            encryptedStudentName + "," + encryptedLibrarianName + "," +
                            encryptedSkey + "," + encryptedLkey + "," + encryptedViolation);
                fwrite.newLine();
            }
        }catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            pause();
            cls();
        }
    }

    public static void saveAccountFD() {
        File fp = new File(Vars.LMS_FD_USER_DETAILS);
        if (!fp.exists()){
            try {
                fp.createNewFile();
            } catch (IOException e) {
                System.out.println("\nERROR: " + e.getMessage());
                pause();
                cls();
            }
        }

        if (Vars.CURRENT_S_KEY == 1 && Vars.CURRENT_L_KEY == 0){ 
            //save student info in flashdrive
            try (BufferedWriter fwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
                for (AccountDetails account : accountList){
                    if (Vars.CURRENT_STUDENT_ID.equals(account.getStudentID())){
                        //-------------------encrypting-------------------
                        String encryptedStudentID = encryptString(account.getStudentID(), KEY);
                        String encryptedLibrarianID = encryptString(account.getLibrarianID(), KEY);
                        String encryptedStudentName = encryptString(account.getStudentName(), KEY);
                        String encryptedLibrarianName = encryptString(account.getLibrarianName(), KEY);
                        int encryptedSkey = encryptInt(account.getSkey(), KEY);
                        int encryptedLkey = encryptInt(account.getLkey(), KEY);
                        int encryptedViolation = encryptInt(account.getViolation(), KEY);
        
                        //-------------------saving-------------------
                        fwrite.write(encryptedStudentID + "," + encryptedLibrarianID + "," +
                                    encryptedStudentName + "," + encryptedLibrarianName + "," +
                                    encryptedSkey + "," + encryptedLkey + "," + encryptedViolation);
                        fwrite.newLine();
                    }
                }
            }catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
                pause();
                cls();
            }

        }else if (Vars.CURRENT_S_KEY == 0 && Vars.CURRENT_L_KEY == 1){ 
            //save librarian info in file
            try (BufferedWriter fwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
                for (AccountDetails account : accountList){
                    if (Vars.CURRENT_LIBRARIAN_ID.equals(account.getLibrarianID())){
                        //-------------------encrypting-------------------
                        String encryptedStudentID = encryptString(account.getStudentID(), KEY);
                        String encryptedLibrarianID = encryptString(account.getLibrarianID(), KEY);
                        String encryptedStudentName = encryptString(account.getStudentName(), KEY);
                        String encryptedLibrarianName = encryptString(account.getLibrarianName(), KEY);
                        int encryptedSkey = encryptInt(account.getSkey(), KEY);
                        int encryptedLkey = encryptInt(account.getLkey(), KEY);
                        int encryptedViolation = encryptInt(account.getViolation(), KEY);
        
                        //-------------------saving-------------------
                        fwrite.write(encryptedStudentID + "," + encryptedLibrarianID + "," +
                                    encryptedStudentName + "," + encryptedLibrarianName + "," +
                                    encryptedSkey + "," + encryptedLkey + "," + encryptedViolation);
                        fwrite.newLine();
                    }
                }
            }catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
                pause();
                cls();
            }
        }else {
            System.out.println("ERROR.");
            System.out.println("UNABLE TO SAVE IN FILE.");
            pause();
            cls();
        }
    }

    public static void saveBooks() {
        File fp = new File(Vars.LMS_BOOKS);
        if (!fp.exists()){
            try {
                fp.createNewFile();
            } catch (IOException e) {
                System.out.println("\nERROR: " + e.getMessage());
                pause();
                cls();
            }
        }

        try (BufferedWriter fwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
            for (BookDetails book : bookList){
                //-------------------encrypting-------------------
                int encryptedBookNum = encryptInt(book.getBookNum(), KEY);
                String encryptedISBN = encryptString(book.getISBN(), KEY);
                String encryptedBookTitle = encryptString(book.getBookTitle(), KEY);
                String encryptedBookAuthor = encryptString(book.getBookAuthor(), KEY);
                int encryptedPubYear = encryptInt(book.getPublicationYear(), KEY);
                int encryptedBookQuant = encryptInt(book.getBookQuantity(), KEY);

                //-------------------saving-------------------
                fwrite.write(encryptedBookNum + "," + encryptedISBN + "," +
                            encryptedBookTitle + "," + encryptedBookAuthor + "," +
                            encryptedPubYear + "," + encryptedBookQuant);
                fwrite.newLine();
            }
        }catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            pause();
            cls();
        }
    }

    public static void saveBookRequests() {
        File fp = new File(Vars.LMS_BOOK_REQUESTS);
        if (!fp.exists()){
            try {
                fp.createNewFile();
            } catch (IOException e) {
                System.out.println("\nERROR: " + e.getMessage());
                pause();
                cls();
            }
        }

        try (BufferedWriter fwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fp), "UTF-8"))) {
            for (BookDetails book : bookRquestList){
                //-------------------encrypting-------------------
                int encryptedBookNum = encryptInt(book.getBookNum(), KEY);
                String encryptedISBN = encryptString(book.getISBN(), KEY);
                String encryptedBookTitle = encryptString(book.getBookTitle(), KEY);
                String encryptedBookAuthor = encryptString(book.getBookAuthor(), KEY);
                int encryptedPubYear = encryptInt(book.getPublicationYear(), KEY);
                int encryptedBookQuant = encryptInt(book.getBookQuantity(), KEY);

                //-------------------saving-------------------
                fwrite.write(encryptedBookNum + "," + encryptedISBN + "," +
                            encryptedBookTitle + "," + encryptedBookAuthor + "," +
                            encryptedPubYear + "," + encryptedBookQuant);
                fwrite.newLine();
            }
        }catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            pause();
            cls();
        }
    }

    public static void logs(String STATUS, String ITEM) {
        //get the current date as a filename
        LocalDate currDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedCurrDate = currDate.format(dateFormatter);

        LocalTime currTime = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedCurrTime = currTime.format(timeFormatter);


        //save to the file path (append)
        File fp = new File(Vars.LMS_LOGS + "\\" + formattedCurrDate + ".csv");

        if (Vars.CURRENT_S_KEY == 1 && Vars.CURRENT_L_KEY == 0){
            //-------------------student-------------------
            try (BufferedWriter fwrite = new BufferedWriter(new FileWriter(fp, true))) {
                fwrite.write(formattedCurrDate + "," + formattedCurrTime + "," + 
                            Vars.CURRENT_STUDENT_NAME + "," + STATUS + "," + ITEM);  
                fwrite.newLine(); 
            }catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
                pause();
                cls();
            }

        }else if (Vars.CURRENT_S_KEY == 0 && Vars.CURRENT_L_KEY == 1){
            //-------------------librarian-------------------
            try (BufferedWriter fwrite = new BufferedWriter(new FileWriter(fp, true))) {
                fwrite.write(formattedCurrDate + "," + formattedCurrTime + "," + 
                            Vars.CURRENT_LIBRARIAN_NAME + "," + STATUS + "," + ITEM);  
                fwrite.newLine(); 
            }catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
                pause();
                cls();
            }
        }
    }

    public static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void pause() {
        System.out.println("\nPRESS ENTER KEY TO CONTINUE...");
        try {
            pause.nextLine();
        } catch (NoSuchElementException e) {
            System.out.println("ERROR: NO INPUT AVAILABLE. THE PROGRAM WILL CONTINUE.");
        }
    }

    //----------------------ENCRYPTION & DECRYPTION----------------------
    private static String encryptString(String value, int encryptionKey) {
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] += encryptionKey;
        }
        return new String(chars);
    }

    // Encrypt an integer value using XOR encryption
    private static int encryptInt(int value, int encryptionKey) {
        return value ^ encryptionKey;
    }

    private static String decryptString(String encryptedValue, int encryptionKey) {
        char[] chars = encryptedValue.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] -= encryptionKey;
        }
        return new String(chars);
    }

    // Decrypt an encrypted integer value using XOR decryption
    private static int decryptInt(int encryptedValue, int encryptionKey) {
        return encryptedValue ^ encryptionKey;
    }


    //----------------------[UI]----------------------

    public static void front()  {
        System.out.println("LIBRARY MANAGEMENT SYSTEM");
    }

    public static void loading() {
        System.out.println("LOADING......");
    }

    public static void scanScreen(int x) {
        if(x==1){
            System.out.println("WELCOME USER!");
        }else{
            System.out.println("NEW USER!");
        }
    }

    public static void exitMessage() {
        System.out.println("LIBRARY MANAGEMENT SYSTEM");
        System.out.println("HOPE TO SEE YOU AGAIN!");
    }

}
