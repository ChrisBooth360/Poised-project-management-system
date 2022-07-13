/**
 * This program is a project management system for a small structural engineering firm called “Poised”.
 * Poised does the engineering needed to ensure the structural integrity of various buildings.
 * <p>
 * The program allows users to view, create, and update various projects.
 * @author Chris Booth
 * @version 1.1
 */

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This is the main class of the program and which contains all the methods used in the main method.
 */
public class Poised {

    // Static final String variables are declared to prevent the same string being written multiple times in the code.
    public static final String INPUT_ERROR = "You have input invalid information. Try Again.";
    public static final String ENTER_COMMAND = "Enter the ";
    public static final String ARCHITECT_STRING = "architect";
    public static final String CONTRACTOR_STRING = "contractor";
    public static final String CUSTOMER_STRING = "customer";
    public static final String ENGINEER_STRING = "structural engineer";
    public static final String MANAGER_STRING = "project manager";
    public static final String PERSON_NAME = "'s name: ";
    public static final String PERSON_PHONE = "'s telephone number: ";
    public static final String PERSON_EMAIL = "'s email address: ";
    public static final String PERSON_ADDRESS = "'s physical address: ";
    public static final String DATABASE_URL = "jdbc:mysql://localhost:3306/poisedpms?useSSL=false";
    public static final String DATABASE_USER = "admin";
    public static final String DATABASE_PASS = "adm1n";
    // This is variable contains a SQL statement that joins all the tables together based on the linked columns.
    public static final String JOIN_TABLES = "SELECT * FROM project_info " +
            "INNER JOIN pay_complete ON project_info.proj_num = pay_complete.proj_num " +
            "INNER JOIN build_info ON project_info.erf_num = build_info.erf_num " +
            "INNER JOIN architect ON project_info.architect = architect.arch_name " +
            "INNER JOIN contractor ON project_info.contractor = contractor.cont_name " +
            "INNER JOIN customer ON project_info.customer = customer.cust_name " +
            "INNER JOIN engineer ON project_info.engineer = engineer.engi_name " +
            "INNER JOIN project_manager ON project_info.project_manager = project_manager.pm_name";

    public static void main(String [] args){

        // A while loop runs until the user enters 'exit'. This while loop will be used as a menu.
        String userChoice = "";
        while (!userChoice.equals("exit")) {
            System.out.println("""
                        Select an option:
                        new - create a new project
                        view - view and update projects
                        search - search and update projects
                        exit - exit the program
                        """);

            Scanner userInput = new Scanner(System.in);
            userChoice = userInput.nextLine();

            // An enhanced switch statement is used to run the appropriate code based on user input.
            switch (userChoice) {
                /*
                If the user enters 'new', then six objects are created that are then used as parameters for
                the new Project object - this is to prevent too many parameters from being passed.
                The four objects are created using the relative methods below.
                */
                case "new" -> {

                    ProjectInfo newProjectInfo = inputNewProjectInfo();
                    Person newArchitect = inputNewPersonInfo(ARCHITECT_STRING);
                    Person newContractor = inputNewPersonInfo(CONTRACTOR_STRING);
                    Person newCustomer = inputNewPersonInfo(CUSTOMER_STRING);
                    Person newEngineer = inputNewPersonInfo(ENGINEER_STRING);
                    Person newManager = inputNewPersonInfo(MANAGER_STRING);

                    Project newProject = new Project(newProjectInfo, newArchitect, newContractor, newCustomer,
                            newEngineer, newManager);

                    // The Project object is printed out and added to the array of Project items.
                    System.out.println(newProject);

                    addProject(newProject);
                }

                /*
                If the user inputs 'view', then the printProjects() method is called with the parameter 'all' which will
                print all projects. The getViewOptions() method is then called to allow the user to refine their view.
                */
                case "view" -> {

                    printProjects("all");

                    getViewOptions();

                }

                //If the user inputs 'search', a method is called that will find the project to update (if it exists).
                case "search" -> searchToUpdate();

                /*
                If the user inputs then 'Goodbye' is printed and the while loop exits, ending the program.
                 */
                case "exit" -> System.out.println("Goodbye!");

                // If the user inputs anything else, this message is shown and the while loop repeats.
                default -> System.out.println(INPUT_ERROR);
            }
        }
    }

    /**
     * The inputNewProjectInfo() method gathers input from the user in order to create a new ProjectInfo object.
     * It takes the final project number from the mySQL table project_info, adds one,
     * and sets it as the project number for the new ProjectInfo object.
     * @return new ProjectInfo object.
     */
    private static ProjectInfo inputNewProjectInfo(){

        Scanner projectInfo = new Scanner(System.in);

        // A try-with-resources block is executed in order to check the connection to the mySQL database,
        // as well as to make sure there are no input mismatches.
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            // A Prepared Statement is used in order to prevent SQL errors or SQL injection.
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM project_info " +
                    "WHERE ? IN (proj_name)")){

            System.out.println("Enter the project name (Optional): ");
            String projectName = projectInfo.nextLine();

            // The ? in the Prepared Statement is set to projectName.
            statement.setString(1, projectName);

            ResultSet projects = statement.executeQuery();

            // The if statement uses the ResultSet to determine if the project name already exists.
            // https://stackoverflow.com/questions/867194/java-resultset-how-to-check-if-there-are-any-results
            if(projects.isBeforeFirst()){

                System.out.println("This project name already exists. Try again.");
                return inputNewProjectInfo();

            }

            System.out.println("Enter the building type: ");
            String building = projectInfo.nextLine();

            System.out.println("Enter the building address: ");
            String buildingAddress = projectInfo.nextLine();

            System.out.println("Enter the ERF Number: ");
            int erf = projectInfo.nextInt();

            System.out.println("Enter the total fee: ");
            double fee = projectInfo.nextInt();
            projectInfo.nextLine();

            System.out.println("Enter the deadline (yyyy-mm-dd): ");
            String deadline = projectInfo.nextLine();
            LocalDate deadlineDate = formatDate(deadline);

            // If the deadline input is before the current date, then a while loop runs until it is set after.
            while(deadlineDate.isBefore(LocalDate.now())){

                System.out.println("The deadline cannot be set to a past date. Try Again.");
                System.out.println("Enter the deadline (yyyy-mm-dd): ");
                deadline = projectInfo.nextLine();
                deadlineDate = formatDate(deadline);

            }

            // If any input information is empty (aside from the optional project name), then this error is called.
            if(building.isEmpty() || buildingAddress.isEmpty() || erf == 0 || fee == 0 || deadline.isEmpty()){

                System.out.println("Make sure you input all relevant information. Try Again.");
                return inputNewProjectInfo();

            }

            // A new ProjectInfo object is created.
            ProjectInfo newProjectInfo = new ProjectInfo(projectName, building, buildingAddress,
                    erf, fee, deadlineDate);

            int projectNumber = 0;

            // The largest project number in the project_info table is determined and one is added to this number.
            projects = statement.executeQuery("SELECT proj_num FROM project_info ORDER BY proj_num DESC LIMIT 1");

            while (projects.next()){
                projectNumber = projects.getInt("proj_num");
            }

            newProjectInfo.setProjectNumber(projectNumber + 1);

            projects.close();

            // If all the input is valid, then the new object is returned.
            return newProjectInfo;

        } catch(InputMismatchException e){

            System.out.println(INPUT_ERROR);
            return inputNewProjectInfo();

        } catch(SQLException e){

            System.out.println("Could not connect to database.");
            System.exit(0);
            return inputNewProjectInfo();

        }
    }

    /**
     * This method gathers input from the user in order to create a new Person object.
     * A String of the Person Type is passed through the method in order to determine
     * which person's information is being inputted.
     * @param personType The type of person for the Person object
     * @return new Person object.
     */
    private static Person inputNewPersonInfo(String personType){

        Scanner personInfo = new Scanner(System.in);

        // A try-catch block is executed in order to make sure the user inputs the correct input.
        try{

            System.out.println(ENTER_COMMAND + personType + PERSON_NAME);
            String personName = personInfo.nextLine();

            System.out.println(ENTER_COMMAND + personType + PERSON_PHONE);
            String personPhone = personInfo.nextLine();

            // The phone number input by the user is validated to make sure it stars with a 0 or a +.
            personPhone = validatePhoneNum(personPhone, personType);

            System.out.println(ENTER_COMMAND + personType + PERSON_EMAIL);
            String personEmail = personInfo.nextLine();

            System.out.println(ENTER_COMMAND + personType + PERSON_ADDRESS);
            String personAddress = personInfo.nextLine();

            // If any input information is empty, then this error is printed and the method is called again.
            if(personName.isEmpty() || personEmail.isEmpty() || personAddress.isEmpty()){

                System.out.println("Make sure you input all relevant information. Try Again.");
                return inputNewPersonInfo(personType);

            }

            // An enhanced switch is used to create the Person object with the appropriate person type.
            return switch (personType) {
                case ARCHITECT_STRING -> new Person(Person.Type.ARCHITECT, personName, personPhone,
                        personEmail, personAddress);
                case CONTRACTOR_STRING -> new Person(Person.Type.CONTRACTOR, personName, personPhone,
                        personEmail, personAddress);
                case CUSTOMER_STRING -> new Person(Person.Type.CUSTOMER, personName, personPhone,
                        personEmail, personAddress);
                case ENGINEER_STRING -> new Person(Person.Type.ENGINEER, personName, personPhone,
                        personEmail, personAddress);
                case MANAGER_STRING -> new Person(Person.Type.MANAGER, personName, personPhone,
                        personEmail, personAddress);
                default -> null;
            };

        } catch(Exception e){
            // If the user inputs anything invalid, then an error is printed and the method is called again.
            System.out.println(INPUT_ERROR);
            return inputNewPersonInfo(personType);
        }
    }

    /**
     * The addProject() method takes a new Project object and inserts it into the PoisedPMS database.
     * It does this by adding the information one table at a time. The project_info table is the last table
     * to have information insert due to the fact that it's a child table of the others.
     * Prepared statements are used inside nested try-with-resources, allowing for safer, more dynamic insertion
     * into the mySQL database.
     * @param newProject The type of person for the Person object
     */
    private static void addProject(Project newProject){

        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS)){

            try(PreparedStatement payComplete = connection.prepareStatement("INSERT INTO pay_complete " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)")){

                payComplete.setInt(1, newProject.projectInfo.getProjectNumber());
                payComplete.setDouble(2, newProject.projectInfo.getTotalFee());
                payComplete.setDouble(3, 0);
                payComplete.setDouble(4, newProject.projectInfo.getTotalOwed());
                payComplete.setString(5, newProject.projectInfo.getDeadline());
                payComplete.setString(6, "N");
                payComplete.setString(7, null);

                payComplete.executeUpdate();

            }

            try(PreparedStatement buildInfo = connection.prepareStatement("INSERT INTO build_info VALUES (?, ?, ?)")){

                buildInfo.setInt(1, newProject.projectInfo.getErfNumber());
                buildInfo.setString(2, newProject.projectInfo.getBuildingType());
                buildInfo.setString(3, newProject.projectInfo.getAddress());

                buildInfo.executeUpdate();

            }

            try(PreparedStatement architectInfo = connection.prepareStatement("INSERT INTO architect " +
                    "VALUES (?, ?, ?, ?)")){

                architectInfo.setString(1, newProject.architect.getName());
                architectInfo.setString(2, newProject.architect.getPhone());
                architectInfo.setString(3, newProject.architect.getEmail());
                architectInfo.setString(4, newProject.architect.getAddress());

                architectInfo.executeUpdate();

            }

            try(PreparedStatement contractorInfo = connection.prepareStatement("INSERT INTO contractor " +
                    "VALUES (?, ?, ?, ?)")){

                contractorInfo.setString(1, newProject.contractor.getName());
                contractorInfo.setString(2, newProject.contractor.getPhone());
                contractorInfo.setString(3, newProject.contractor.getEmail());
                contractorInfo.setString(4, newProject.contractor.getAddress());

                contractorInfo.executeUpdate();

            }

            try(PreparedStatement customerInfo = connection.prepareStatement("INSERT INTO customer " +
                    "VALUES (?, ?, ?, ?)")){

                customerInfo.setString(1, newProject.customer.getName());
                customerInfo.setString(2, newProject.customer.getPhone());
                customerInfo.setString(3, newProject.customer.getEmail());
                customerInfo.setString(4, newProject.customer.getAddress());

                customerInfo.executeUpdate();

            }

            try(PreparedStatement engineerInfo = connection.prepareStatement("INSERT INTO engineer " +
                    "VALUES (?, ?, ?, ?)")){

                engineerInfo.setString(1, newProject.engineer.getName());
                engineerInfo.setString(2, newProject.engineer.getPhone());
                engineerInfo.setString(3, newProject.engineer.getEmail());
                engineerInfo.setString(4, newProject.engineer.getAddress());

                engineerInfo.executeUpdate();

            }

            try(PreparedStatement managerInfo = connection.prepareStatement("INSERT INTO project_manager " +
                    "VALUES (?, ?, ?, ?)")){

                managerInfo.setString(1, newProject.manager.getName());
                managerInfo.setString(2, newProject.manager.getPhone());
                managerInfo.setString(3, newProject.manager.getEmail());
                managerInfo.setString(4, newProject.manager.getAddress());

                managerInfo.executeUpdate();

            }

            try(PreparedStatement projectInfo = connection.prepareStatement("INSERT INTO project_info " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")){

                projectInfo.setInt(1, newProject.projectInfo.getProjectNumber());
                projectInfo.setString(2, newProject.projectInfo.getProjectName());
                projectInfo.setInt(3, newProject.projectInfo.getErfNumber());
                projectInfo.setString(4, newProject.architect.getName());
                projectInfo.setString(5, newProject.contractor.getName());
                projectInfo.setString(6, newProject.customer.getName());
                projectInfo.setString(7, newProject.engineer.getName());
                projectInfo.setString(8, newProject.manager.getName());

                projectInfo.executeUpdate();

            }

        }catch (Exception e){
            // If there are any errors with inserting the data in any of the try statements, this error occurs.
            System.out.println("Could not add to database.");

        }
    }

    /**
     * This method allows the user to further refine the projects they would like to view.
     * It allows them to view incomplete projects and overdue projects.
     */
    private static void getViewOptions(){

        String userViewChoice = "";
        while (!userViewChoice.equals("back")) {
            System.out.println("""
                        Select an option:
                        incomplete - view all incomplete projects
                        overdue - view all overdue projects
                        back - go back""");
            Scanner updateChoice = new Scanner(System.in);
            userViewChoice = updateChoice.nextLine();

            switch (userViewChoice) {

                // In this case, 'incomplete' is passed as a parameter for the printProjects() method.
                case "incomplete" -> printProjects("incomplete");

                // In this case, 'overdue' is passed as a parameter for the printProjects() method.
                case "overdue" -> printProjects("overdue");

                // In this case, a blank line is printed and the while loop exits.
                case "back" -> System.out.println();

                // If the user inputs anything else, this message is shown and the while loop repeats.
                default -> System.out.println(INPUT_ERROR);
            }
        }
    }

    /**
     * The printProjects() method prints out either all, incomplete, or overdue projects, depending on the parameter
     * passed through the method. It does this by first checking the parameter and creating a ResultSet.
     * This ResultSet is then used to create an Array of Projects and if the array is not empty,
     * then all projects are printed.
     * @param printParameter This parameter is used to define which projects will be printed.
     */
    private static void printProjects(String printParameter){

        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            Statement statement = connection.createStatement()){

            ResultSet selectedRows;

            // If the parameter is 'overdue',the ResultSet will include projects with deadlines before the current date.
            if(printParameter.equals("overdue")){

                LocalDate today = LocalDate.now();

                String dateToday = today.toString();

                selectedRows = statement.executeQuery(JOIN_TABLES + " WHERE pay_complete.deadline < '"
                        + dateToday + "'");

            }
            // If the parameter is 'incomplete', the ResultSet will include projects where the finalised column is 'N'.
            else if (printParameter.equals("incomplete")) {

                selectedRows = statement.executeQuery(JOIN_TABLES + " WHERE finalised='N'");

            }
            // If the parameter is anything else, then all projects are added to the ResultSet.
            else{

                selectedRows = statement.executeQuery(JOIN_TABLES);

            }

            ArrayList<Project> listOfProjects = createProjectObjects(selectedRows);

            // If the array list is empty, then a variety of possible error statements are printed.
            // Otherwise, the projects in the array are printed.
            if (listOfProjects.isEmpty() && printParameter.equals("overdue")) {

                System.out.println("No overdue projects found.");

            } else if(listOfProjects.isEmpty() && printParameter.equals("incomplete")) {

                System.out.println("No incomplete projects found.");

            }else if(listOfProjects.isEmpty() && printParameter.equals("all")){

                System.out.println("No projects found.");

            }else{

                for(Project project : listOfProjects){

                    System.out.println(project);

                }
            }

            selectedRows.close();

        } catch(Exception e){

            System.out.println("Could not add to database.");

        }
    }

    /**
     * The searchToUpdate() method allows the user to search for a Project to update using its project name or number.
     */
    private static void searchToUpdate(){

        // A Prepared Statement is used during the search to allow for safer, dynamic insertion into the mySQL database.
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            PreparedStatement statement = connection.prepareStatement(JOIN_TABLES +
                    " WHERE project_info.proj_num = ? OR proj_name = ?")){

            // The user is asked to enter a project name or number, or to enter 'back' to exit the search function.
            Scanner userInput = new Scanner(System.in);
            System.out.println("Enter the name or number of the project you wish to update (Enter 'back' to exit): ");
            String searchString = userInput.nextLine();

            // A ResultSet, an empty integer and a placeholder statement is declared.
            ResultSet selectedRows;
            int projID;
            statement.setInt(1, 0);

            // If the search string is numerical, then it is parsed into an integer set as the project number.
            if(isNumeric(searchString)){

                projID = Integer.parseInt(searchString);

                statement.setInt(1, projID);

            }

            // The search string is set as the project name.
            statement.setString(2, searchString);

            selectedRows = statement.executeQuery();

            // An array list of all projects in the ResultSet are created.
            ArrayList<Project> projectList = createProjectObjects(selectedRows);

            // If the array list is empty, then the project doesn't exist and the user is told so.
            if (projectList.isEmpty()) {

                System.out.println("No project found. Try again.");

            }
            // If the array has one project in, the project is printed and the getUpdateOptions() method is called.
            else if (projectList.size() == 1){

                for(Project project : projectList){

                    System.out.println(project);

                }

                getUpdateOptions(projectList.get(0));

            }
            // If the array has more than one project, then they are printed out and the user is told to be more
            // specific in their search.
            else{

                for(Project project : projectList){

                    System.out.println(project);

                }

                System.out.println("Multiple projects found. Please refine your search " +
                        "with the appropriate project number.");
            }

            selectedRows.close();

        } catch(Exception e){

            System.out.println("Could not connect to database.");

        }
    }

    /**
     * This method lets the user choose which aspect of a project they would like to update.
     * @param projectToUpdate the project that will be updated
     */
    private static void getUpdateOptions(Project projectToUpdate){

        // A menu is printed to allow the user to input what aspect of the project they would like to update.
        boolean exit = false;
        while (!exit) {
            System.out.println("""
                        Select an option:
                        project - update project info
                        architect - update architect info
                        contractor - update contractor info
                        customer - update customer info
                        engineer - update structural engineer info
                        manager - update project manager info
                        f - finalise project
                        delete - delete project
                        search - search for another project
                        back - go back""");
            Scanner updateChoice = new Scanner(System.in);
            String userUpdateChoice = updateChoice.nextLine();

            switch (userUpdateChoice) {

                // In this case, the updateProjectInfo() method is called with the project as its argument.
                case "project" -> updateProjectInfo(projectToUpdate);


                // In these cases, the user is presented with more update options.
                case ARCHITECT_STRING, CONTRACTOR_STRING, CUSTOMER_STRING, "engineer", "manager" -> {

                    System.out.println("What " + userUpdateChoice + " information would you like to update?\n" +
                            "all - update all " + userUpdateChoice + "'s information\n" +
                            "name - update " + userUpdateChoice + "'s name\n" +
                            "phone - update " + userUpdateChoice + "'s phone number\n" +
                            "email - update " + userUpdateChoice + "'s email address\n" +
                            "address - update " + userUpdateChoice + "'s address\n" +
                            "back - go back");

                    String updateAspect = updateChoice.nextLine();

                    // The appropriate method will run based on the user input.
                    switch (userUpdateChoice) {
                        case ARCHITECT_STRING -> updateArchitect(projectToUpdate, updateAspect);
                        case CONTRACTOR_STRING -> updateContractor(projectToUpdate, updateAspect);
                        case CUSTOMER_STRING -> updateCustomer(projectToUpdate, updateAspect);
                        case "engineer" -> updateEngineer(projectToUpdate, updateAspect);
                        case "manager" -> updateManager(projectToUpdate, updateAspect);
                        default -> {
                            System.out.println("Invalid input. Try again.");
                            getUpdateOptions(projectToUpdate);
                        }

                    }
                }
                // In this case, the finalise() method is called and the while loop exits.
                case "f" -> {
                    finalise(projectToUpdate);

                    // The while loop then exits.
                    exit = true;
                }
                // In this case, deleteProject() runs and 'exit' is set to true or false from the method's return.
                case "delete" -> exit = deleteProject(projectToUpdate);
                // In this case, the user can search for a different project.
                case "search" -> searchToUpdate();
                // If the user inputs 'back', then the while loop exits.
                case "back" -> exit = true;
                default -> System.out.println(INPUT_ERROR);
            }
        }
    }
    /**
     * The updateProjectInfo() method allows a user select which aspect of the project information they would like
     * to update. It then uses prepared statements and user's input to update that information.
     * The project number, total owed, and the complete date of the project cannot be updated in this method.
     * @param projectToUpdate the project that will be updated
     */
    private static void updateProjectInfo(Project projectToUpdate){

        Scanner projectInfoChoice = new Scanner(System.in);
        boolean exit = false;
        while(!exit){

            System.out.println("""
                            What project information would you like to update?
                            name - project name
                            type - building type
                            address - address
                            fee - Total Fee
                            paid - Total Paid
                            deadline - deadline
                            back - go back""");

            String projectAspect = projectInfoChoice.nextLine();

            // A try-with-resource block is used containing various prepared statements to help update the tables
            // in the mySQL database in a dynamic way.
            try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
                PreparedStatement projectName = connection.prepareStatement("UPDATE project_info SET proj_name = ? " +
                        "WHERE proj_num = ?");
                PreparedStatement buildType = connection.prepareStatement("UPDATE build_info SET build_type = ? " +
                        "WHERE erf_num = ?");
                PreparedStatement buildAddress = connection.prepareStatement("UPDATE build_info SET build_address = ? " +
                        "WHERE erf_num = ?");
                PreparedStatement fee = connection.prepareStatement("UPDATE pay_complete SET total_fee = ? " +
                        "WHERE proj_num = ?");
                PreparedStatement owed = connection.prepareStatement("UPDATE pay_complete SET total_owed = ? " +
                        "WHERE proj_num = ?");
                PreparedStatement paid = connection.prepareStatement("UPDATE pay_complete SET total_paid = ? " +
                        "WHERE proj_num = ?");
                PreparedStatement deadline = connection.prepareStatement("UPDATE pay_complete SET deadline = ? " +
                        "WHERE proj_num = ?")){

                switch (projectAspect){

                    // Depending on the input, the relevant aspect will be updated in the appropriate table
                    // and updated in the project object.
                    case "name":
                        System.out.println("Enter the new project name:");
                        String userUpdateChoice = projectInfoChoice.nextLine();

                        projectName.setString(1, userUpdateChoice);
                        projectName.setInt(2, projectToUpdate.projectInfo.getProjectNumber());

                        projectName.executeUpdate();

                        projectToUpdate.projectInfo.setProjectName(userUpdateChoice);

                        break;
                    case "type":
                        System.out.println("Enter the new building type:");
                        userUpdateChoice = projectInfoChoice.nextLine();

                        buildType.setString(1, userUpdateChoice);
                        buildType.setInt(2, projectToUpdate.projectInfo.getErfNumber());

                        buildType.executeUpdate();

                        projectToUpdate.projectInfo.setBuildingType(userUpdateChoice);

                        break;
                    case "address":
                        System.out.println("Enter the address:");
                        userUpdateChoice = projectInfoChoice.nextLine();

                        buildAddress.setString(1, userUpdateChoice);
                        buildAddress.setInt(2, projectToUpdate.projectInfo.getErfNumber());
                        buildAddress.executeUpdate();

                        projectToUpdate.projectInfo.setAddress(userUpdateChoice);

                        break;
                    case "fee":
                        System.out.println("Enter the new fee:");
                        double userUpdateChoice2 = projectInfoChoice.nextDouble();

                        fee.setDouble(1, userUpdateChoice2);
                        fee.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                        fee.executeUpdate();

                        projectToUpdate.projectInfo.setTotalFee(userUpdateChoice2);

                        owed.setDouble(1, projectToUpdate.projectInfo.getTotalOwed());
                        owed.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                        owed.executeUpdate();

                        break;

                    case "paid":
                        // The total paid is calculated by adding the new fee paid to the fee already paid.
                        System.out.println("The current amount paid out of R" +
                                String.format("%.2f", projectToUpdate.projectInfo.getTotalFee()) +
                                ": R" + String.format("%.2f", projectToUpdate.projectInfo.getTotalPaid()));

                        // The user inputs the amount to be paid.
                        System.out.println("Enter amount paid: ");
                        double newFeePaid = projectInfoChoice.nextDouble();

                        double newTotalPaid = newFeePaid + projectToUpdate.projectInfo.getTotalPaid();

                        paid.setDouble(1, newTotalPaid);
                        paid.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                        paid.executeUpdate();

                        projectToUpdate.projectInfo.setTotalPaid(newTotalPaid);

                        owed.setDouble(1, projectToUpdate.projectInfo.getTotalOwed());
                        owed.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                        owed.executeUpdate();

                        break;

                    case "deadline":
                        System.out.println("The current due date is: " + projectToUpdate.projectInfo.getDeadline()
                                + "\nEnter new due date (yyyy-mm-dd): ");
                        userUpdateChoice = projectInfoChoice.nextLine();

                        // The input is converted to a LocalDate variable and returned.
                        LocalDate newDeadlineDate = formatDate(userUpdateChoice);

                        deadline.setString(1, userUpdateChoice);
                        deadline.setInt(2, projectToUpdate.projectInfo.getProjectNumber());

                        deadline.executeUpdate();

                        projectToUpdate.projectInfo.setDeadline(newDeadlineDate);

                        break;

                    // If the user inputs 'back', then the switch-case is broken and the original menu is displayed.
                    case "back":

                        exit = true;

                        break;
                    // If any input is invalid, then this error will print.
                    default:

                        System.out.println(INPUT_ERROR);

                        break;
                }
            } catch(InputMismatchException e){

                System.out.println(INPUT_ERROR);
                updateProjectInfo(projectToUpdate);

            } catch(SQLException e){

                System.out.println("Could not connect to database.");
                System.exit(0);
                updateProjectInfo(projectToUpdate);

            }
            // The project is printed out with the updated information.
            System.out.println(projectToUpdate);
        }
    }

    /**
     * The updateArchitect() method updates the relevant information about the project's architect.
     * @param projectToUpdate the project that will be updated
     * @param updateAspect the type of information being updated - ie name, phone, email, and address.
     */
    private static void updateArchitect(Project projectToUpdate, String updateAspect){

        // A try-with-resource block is used containing various prepared statements to help update the architect table
        // in the mySQL database in a dynamic way.
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            PreparedStatement insertAll = connection.prepareStatement("INSERT INTO architect VALUES (?, ?, ? ,?)");
            PreparedStatement updateProjectInfo = connection.prepareStatement("UPDATE project_info " +
                    "SET architect = ? WHERE proj_num = ?");
            PreparedStatement deleteItem = connection.prepareStatement("DELETE FROM architect WHERE arch_name = ?");
            PreparedStatement updatePhone = connection.prepareStatement("UPDATE architect SET arch_tele = ? " +
                    "WHERE arch_name = ?");
            PreparedStatement updateEmail = connection.prepareStatement("UPDATE architect SET arch_email = ? " +
                    "WHERE arch_name = ?");
            PreparedStatement updateAddress = connection.prepareStatement("UPDATE architect SET arch_address = ? " +
                    "WHERE arch_name = ?")){

            Scanner personInfoChoice = new Scanner(System.in);

            switch(updateAspect){

                // If the update aspect is 'all', then the method itself is called four times with all the parameters
                // that can be entered. This therefore updates all the aspects of the architect.
                case "all" -> {

                    updateArchitect(projectToUpdate, "name");
                    updateArchitect(projectToUpdate, "phone");
                    updateArchitect(projectToUpdate, "email");
                    updateArchitect(projectToUpdate, "address");

                }
                // As an architect's name is a primary key and a secondary key, a new entry needs to be inserted,
                // the related table needs to be updated and the original entry needs to be deleted.
                case "name" -> {

                    String originalName = projectToUpdate.architect.getName();

                    System.out.println(ENTER_COMMAND + "new " + ARCHITECT_STRING + PERSON_NAME);
                    String newArchitectName = personInfoChoice.nextLine();

                    if(newArchitectName.isEmpty()){

                        System.out.println("Make sure you input a name. Try Again.");
                        updateArchitect(projectToUpdate, updateAspect);

                    }

                    insertAll.setString(1, newArchitectName);
                    insertAll.setString(2, projectToUpdate.architect.getPhone());
                    insertAll.setString(3, projectToUpdate.architect.getEmail());
                    insertAll.setString(4, projectToUpdate.architect.getAddress());
                    insertAll.executeUpdate();

                    updateProjectInfo.setString(1, newArchitectName);
                    updateProjectInfo.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                    updateProjectInfo.executeUpdate();

                    deleteItem.setString(1, originalName);
                    deleteItem.executeUpdate();

                    projectToUpdate.architect.setName(newArchitectName);

                }
                // The architect's phone is updated after being validated as a phone number starting with a 0 or +.
                case "phone" -> {

                    System.out.println(ENTER_COMMAND + ARCHITECT_STRING + PERSON_PHONE);
                    String newArchitectPhone = personInfoChoice.nextLine();

                    newArchitectPhone = validatePhoneNum(newArchitectPhone, ARCHITECT_STRING);

                    updatePhone.setString(1, newArchitectPhone);
                    updatePhone.setString(2, projectToUpdate.architect.getName());
                    updatePhone.executeUpdate();

                    projectToUpdate.architect.setPhone(newArchitectPhone);

                }
                // The architect's email is updated.
                case "email" -> {

                    System.out.println(ENTER_COMMAND + ARCHITECT_STRING + PERSON_EMAIL);
                    String newArchitectEmail = personInfoChoice.nextLine();

                    updateEmail.setString(1, newArchitectEmail);
                    updateEmail.setString(2, projectToUpdate.architect.getName());
                    updateEmail.executeUpdate();

                    projectToUpdate.architect.setEmail(newArchitectEmail);

                }
                // The architect's address is updated.
                case "address" -> {

                    System.out.println(ENTER_COMMAND + ARCHITECT_STRING + PERSON_ADDRESS);
                    String newArchitectAddress = personInfoChoice.nextLine();

                    updateAddress.setString(1, newArchitectAddress);
                    updateAddress.setString(2, projectToUpdate.architect.getName());
                    updateAddress.executeUpdate();

                    projectToUpdate.architect.setAddress(newArchitectAddress);

                }
                // In this case, the previous method is called.
                case "back" -> getUpdateOptions(projectToUpdate);

                default -> {

                    System.out.println("You have input invalid information. Try Again.");
                    getUpdateOptions(projectToUpdate);

                }
            }
        } catch(Exception e){
            System.out.println("Could not connect to database.");
        }
        // The project is printed out with the updated information.
        System.out.println(projectToUpdate);
    }

    /**
     * The updateContractor() method updates the relevant information about the project's contractor.
     * @param projectToUpdate the project that will be updated
     * @param updateAspect the type of information being updated - ie name, phone, email, and address.
     */
    private static void updateContractor(Project projectToUpdate, String updateAspect){

        // A try-with-resource block is used containing various prepared statements to help update the contractor table
        // in the mySQL database in a dynamic way.
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            PreparedStatement insertAll = connection.prepareStatement("INSERT INTO contractor VALUES (?, ?, ? ,?)");
            PreparedStatement updateProjectInfo = connection.prepareStatement("UPDATE project_info " +
                    "SET contractor = ? WHERE proj_num = ?");
            PreparedStatement deleteItem = connection.prepareStatement("DELETE FROM contractor WHERE cont_name = ?");
            PreparedStatement updatePhone = connection.prepareStatement("UPDATE contractor SET cont_tele = ? " +
                    "WHERE cont_name = ?");
            PreparedStatement updateEmail = connection.prepareStatement("UPDATE contractor SET cont_email = ? " +
                    "WHERE cont_name = ?");
            PreparedStatement updateAddress = connection.prepareStatement("UPDATE contractor SET cont_address = ? " +
                    "WHERE cont_name = ?")){

            Scanner personInfoChoice = new Scanner(System.in);

            switch(updateAspect){

                // If the update aspect is 'all', then the method itself is called four times with all the parameters
                // that can be entered. This therefore updates all the aspects of the contractor.
                case "all" -> {

                    updateContractor(projectToUpdate, "name");
                    updateContractor(projectToUpdate, "phone");
                    updateContractor(projectToUpdate, "email");
                    updateContractor(projectToUpdate, "address");

                }
                // As a contractor's name is a primary key and a secondary key, a new entry needs to be inserted,
                // the related table needs to be updated and the original entry needs to be deleted.
                case "name" -> {

                    String originalName = projectToUpdate.contractor.getName();

                    System.out.println(ENTER_COMMAND + "new " + CONTRACTOR_STRING + PERSON_NAME);
                    String newContractorName = personInfoChoice.nextLine();

                    if(newContractorName.isEmpty()){

                        System.out.println("Make sure you input all relevant information. Try Again.");
                        updateContractor(projectToUpdate, updateAspect);

                    }

                    insertAll.setString(1, newContractorName);
                    insertAll.setString(2, projectToUpdate.contractor.getPhone());
                    insertAll.setString(3, projectToUpdate.contractor.getEmail());
                    insertAll.setString(4, projectToUpdate.contractor.getAddress());
                    insertAll.executeUpdate();

                    updateProjectInfo.setString(1, newContractorName);
                    updateProjectInfo.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                    updateProjectInfo.executeUpdate();

                    deleteItem.setString(1, originalName);
                    deleteItem.executeUpdate();

                    projectToUpdate.contractor.setName(newContractorName);

                }
                // The contractor's phone is updated after being validated as a phone number starting with a 0 or +.
                case "phone" -> {

                    System.out.println(ENTER_COMMAND + CONTRACTOR_STRING + PERSON_PHONE);
                    String newContractorPhone = personInfoChoice.nextLine();

                    newContractorPhone = validatePhoneNum(newContractorPhone, CONTRACTOR_STRING);

                    updatePhone.setString(1, newContractorPhone);
                    updatePhone.setString(2, projectToUpdate.contractor.getName());
                    updatePhone.executeUpdate();

                    projectToUpdate.contractor.setPhone(newContractorPhone);

                }
                // The contractor's email is updated.
                case "email" -> {

                    System.out.println(ENTER_COMMAND + CONTRACTOR_STRING + PERSON_EMAIL);
                    String newContractorEmail = personInfoChoice.nextLine();

                    updateEmail.setString(1, newContractorEmail);
                    updateEmail.setString(2, projectToUpdate.contractor.getName());
                    updateEmail.executeUpdate();

                    projectToUpdate.contractor.setEmail(newContractorEmail);

                }
                // The contractor's address is updated.
                case "address" -> {

                    System.out.println(ENTER_COMMAND + CONTRACTOR_STRING + PERSON_ADDRESS);
                    String newContractorAddress = personInfoChoice.nextLine();

                    updateAddress.setString(1, newContractorAddress);
                    updateAddress.setString(2, projectToUpdate.contractor.getName());
                    updateAddress.executeUpdate();

                    projectToUpdate.contractor.setAddress(newContractorAddress);

                }
                // In this case, the previous method is called.
                case "back" -> getUpdateOptions(projectToUpdate);
                default -> {

                    System.out.println("You have input invalid information. Try Again.");
                    getUpdateOptions(projectToUpdate);

                }
            }
        } catch(Exception e){
            System.out.println("Invalid input. Try again.");
        }
        // The project is printed out with the updated information.
        System.out.println(projectToUpdate);
    }

    /**
     * The updateCustomer() method updates the relevant information about the project's customer.
     * @param projectToUpdate the project that will be updated
     * @param updateAspect the type of information being updated - ie name, phone, email, and address.
     */
    private static void updateCustomer(Project projectToUpdate, String updateAspect){

        // A try-with-resource block is used containing various prepared statements to help update the customer table
        // in the mySQL database in a dynamic way.
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            PreparedStatement insertAll = connection.prepareStatement("INSERT INTO customer VALUES (?, ?, ? ,?)");
            PreparedStatement updateProjectInfo = connection.prepareStatement("UPDATE project_info " +
                    "SET customer = ? WHERE proj_num = ?");
            PreparedStatement deleteItem = connection.prepareStatement("DELETE FROM customer WHERE cust_name = ?");
            PreparedStatement updatePhone = connection.prepareStatement("UPDATE customer SET cust_tele = ? " +
                    "WHERE cust_name = ?");
            PreparedStatement updateEmail = connection.prepareStatement("UPDATE customer SET cust_email = ? " +
                    "WHERE cust_name = ?");
            PreparedStatement updateAddress = connection.prepareStatement("UPDATE customer SET cust_address = ? " +
                    "WHERE cust_name = ?")){

            Scanner personInfoChoice = new Scanner(System.in);

            switch(updateAspect){

                // If the update aspect is 'all', then the method itself is called four times with all the parameters
                // that can be entered. This therefore updates all the aspects of the customer.
                case "all" -> {

                    updateCustomer(projectToUpdate, "name");
                    updateCustomer(projectToUpdate, "phone");
                    updateCustomer(projectToUpdate, "email");
                    updateCustomer(projectToUpdate, "address");

                }
                // As a customer's name is a primary key and a secondary key, a new entry needs to be inserted,
                // the related table needs to be updated and the original entry needs to be deleted.
                case "name" -> {

                    String originalName = projectToUpdate.customer.getName();

                    System.out.println(ENTER_COMMAND + "new " + CUSTOMER_STRING + PERSON_NAME);
                    String newCustomerName = personInfoChoice.nextLine();

                    if(newCustomerName.isEmpty()){

                        System.out.println("Make sure you input all relevant information. Try Again.");
                        updateCustomer(projectToUpdate, updateAspect);

                    }

                    insertAll.setString(1, newCustomerName);
                    insertAll.setString(2, projectToUpdate.customer.getPhone());
                    insertAll.setString(3, projectToUpdate.customer.getEmail());
                    insertAll.setString(4, projectToUpdate.customer.getAddress());
                    insertAll.executeUpdate();

                    updateProjectInfo.setString(1, newCustomerName);
                    updateProjectInfo.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                    updateProjectInfo.executeUpdate();

                    deleteItem.setString(1, originalName);
                    deleteItem.executeUpdate();

                    projectToUpdate.customer.setName(newCustomerName);

                }
                // The customer's phone is updated after being validated as a phone number starting with a 0 or +.
                case "phone" -> {

                    System.out.println(ENTER_COMMAND + CUSTOMER_STRING + PERSON_PHONE);
                    String newCustomerPhone = personInfoChoice.nextLine();

                    newCustomerPhone = validatePhoneNum(newCustomerPhone, CUSTOMER_STRING);

                    updatePhone.setString(1, newCustomerPhone);
                    updatePhone.setString(2, projectToUpdate.customer.getName());
                    updatePhone.executeUpdate();

                    projectToUpdate.customer.setPhone(newCustomerPhone);

                }
                // The customer's email is updated.
                case "email" -> {

                    System.out.println(ENTER_COMMAND + CUSTOMER_STRING + PERSON_EMAIL);
                    String newCustomerEmail = personInfoChoice.nextLine();

                    updateEmail.setString(1, newCustomerEmail);
                    updateEmail.setString(2, projectToUpdate.customer.getName());
                    updateEmail.executeUpdate();

                    projectToUpdate.customer.setEmail(newCustomerEmail);

                }
                // The customer's address is updated.
                case "address" -> {

                    System.out.println(ENTER_COMMAND + CUSTOMER_STRING + PERSON_ADDRESS);
                    String newCustomerAddress = personInfoChoice.nextLine();

                    updateAddress.setString(1, newCustomerAddress);
                    updateAddress.setString(2, projectToUpdate.customer.getName());
                    updateAddress.executeUpdate();

                    projectToUpdate.customer.setAddress(newCustomerAddress);

                }
                // In this case, the previous method is called.
                case "back" -> getUpdateOptions(projectToUpdate);
                default -> {

                    System.out.println("You have input invalid information. Try Again.");
                    getUpdateOptions(projectToUpdate);

                }
            }
        } catch(Exception e){
            System.out.println("Invalid input. Try again.");
        }
        // The project is printed out with the updated information.
        System.out.println(projectToUpdate);
    }

    /**
     * The updateEngineer() method updates the relevant information about the project's structural engineer.
     * @param projectToUpdate the project that will be updated
     * @param updateAspect the type of information being updated - ie name, phone, email, and address.
     */
    private static void updateEngineer(Project projectToUpdate, String updateAspect){

        // A try-with-resource block is used containing various prepared statements to help update the engineer table
        // in the mySQL database in a dynamic way.
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            PreparedStatement insertAll = connection.prepareStatement("INSERT INTO engineer VALUES (?, ?, ? ,?)");
            PreparedStatement updateProjectInfo = connection.prepareStatement("UPDATE project_info " +
                    "SET engineer = ? WHERE proj_num = ?");
            PreparedStatement deleteItem = connection.prepareStatement("DELETE FROM engineer WHERE engi_name = ?");
            PreparedStatement updatePhone = connection.prepareStatement("UPDATE engineer SET engi_tele = ? " +
                    "WHERE engi_name = ?");
            PreparedStatement updateEmail = connection.prepareStatement("UPDATE engineer SET engi_email = ? " +
                    "WHERE engi_name = ?");
            PreparedStatement updateAddress = connection.prepareStatement("UPDATE engineer SET engi_address = ? " +
                    "WHERE engi_name = ?")){

            Scanner personInfoChoice = new Scanner(System.in);

            switch(updateAspect){

                // If the update aspect is 'all', then the method itself is called four times with all the parameters
                // that can be entered. This therefore updates all the aspects of the engineer.
                case "all" -> {

                    updateEngineer(projectToUpdate, "name");
                    updateEngineer(projectToUpdate, "phone");
                    updateEngineer(projectToUpdate, "email");
                    updateEngineer(projectToUpdate, "address");

                }
                // As an engineer's name is a primary key and a secondary key, a new entry needs to be inserted,
                // the related table needs to be updated and the original entry needs to be deleted.
                case "name" -> {

                    String originalName = projectToUpdate.engineer.getName();

                    System.out.println(ENTER_COMMAND + "new " + ENGINEER_STRING + PERSON_NAME);
                    String newEngineerName = personInfoChoice.nextLine();

                    if(newEngineerName.isEmpty()){

                        System.out.println("Make sure you input all relevant information. Try Again.");
                        updateEngineer(projectToUpdate, updateAspect);

                    }

                    insertAll.setString(1, newEngineerName);
                    insertAll.setString(2, projectToUpdate.engineer.getPhone());
                    insertAll.setString(3, projectToUpdate.engineer.getEmail());
                    insertAll.setString(4, projectToUpdate.engineer.getAddress());
                    insertAll.executeUpdate();

                    updateProjectInfo.setString(1, newEngineerName);
                    updateProjectInfo.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                    updateProjectInfo.executeUpdate();

                    deleteItem.setString(1, originalName);
                    deleteItem.executeUpdate();

                    projectToUpdate.engineer.setName(newEngineerName);

                }
                // The engineer's phone is updated after being validated as a phone number starting with a 0 or +.
                case "phone" -> {

                    System.out.println(ENTER_COMMAND + ENGINEER_STRING + PERSON_PHONE);
                    String newEngineerPhone = personInfoChoice.nextLine();

                    newEngineerPhone = validatePhoneNum(newEngineerPhone, ENGINEER_STRING);

                    updatePhone.setString(1, newEngineerPhone);
                    updatePhone.setString(2, projectToUpdate.engineer.getName());
                    updatePhone.executeUpdate();

                    projectToUpdate.engineer.setPhone(newEngineerPhone);

                }
                // The engineer's email is updated.
                case "email" -> {

                    System.out.println(ENTER_COMMAND + ENGINEER_STRING + PERSON_EMAIL);
                    String newEngineerEmail = personInfoChoice.nextLine();

                    updateEmail.setString(1, newEngineerEmail);
                    updateEmail.setString(2, projectToUpdate.engineer.getName());
                    updateEmail.executeUpdate();

                    projectToUpdate.engineer.setEmail(newEngineerEmail);

                }
                // The engineer's address is updated.
                case "address" -> {

                    System.out.println(ENTER_COMMAND + ENGINEER_STRING + PERSON_ADDRESS);
                    String newEngineerAddress = personInfoChoice.nextLine();

                    updateAddress.setString(1, newEngineerAddress);
                    updateAddress.setString(2, projectToUpdate.engineer.getName());
                    updateAddress.executeUpdate();

                    projectToUpdate.engineer.setAddress(newEngineerAddress);

                }
                // In this case, the previous method is called.
                case "back" -> getUpdateOptions(projectToUpdate);
                default -> {

                    System.out.println("You have input invalid information. Try Again.");
                    getUpdateOptions(projectToUpdate);

                }
            }
        } catch(Exception e){
            System.out.println("Invalid input. Try again.");
        }
        // The project is printed out with the updated information.
        System.out.println(projectToUpdate);
    }

    /**
     * The updateManager() method updates the relevant information about the project's project manager.
     * @param projectToUpdate the project that will be updated
     * @param updateAspect the type of information being updated - ie name, phone, email, and address.
     */
    private static void updateManager(Project projectToUpdate, String updateAspect){

        // A try-with-resource block is used containing various prepared statements to help update the manager table
        // in the mySQL database in a dynamic way.
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            PreparedStatement insertAll = connection.prepareStatement("INSERT INTO project_manager " +
                    "VALUES (?, ?, ? ,?)");
            PreparedStatement updateProjectInfo = connection.prepareStatement("UPDATE project_info " +
                    "SET project_manager = ? WHERE proj_num = ?");
            PreparedStatement deleteItem = connection.prepareStatement("DELETE FROM project_manager " +
                    "WHERE pm_name = ?");
            PreparedStatement updatePhone = connection.prepareStatement("UPDATE project_manager SET pm_tele = ? " +
                    "WHERE pm_name = ?");
            PreparedStatement updateEmail = connection.prepareStatement("UPDATE project_manager SET pm_email = ? " +
                    "WHERE pm_name = ?");
            PreparedStatement updateAddress = connection.prepareStatement("UPDATE project_manager SET pm_address = ? " +
                    "WHERE pm_name = ?")){

            Scanner personInfoChoice = new Scanner(System.in);

            switch(updateAspect){

                // If the update aspect is 'all', then the method itself is called four times with all the parameters
                // that can be entered. This therefore updates all the aspects of the manager.
                case "all" -> {

                    updateManager(projectToUpdate, "name");
                    updateManager(projectToUpdate, "phone");
                    updateManager(projectToUpdate, "email");
                    updateManager(projectToUpdate, "address");

                }
                // As a manager's name is a primary key and a secondary key, a new entry needs to be inserted,
                // the related table needs to be updated and the original entry needs to be deleted.
                case "name" -> {

                    String originalName = projectToUpdate.manager.getName();

                    System.out.println(ENTER_COMMAND + "new " + MANAGER_STRING + PERSON_NAME);
                    String newManagerName = personInfoChoice.nextLine();

                    if(newManagerName.isEmpty()){

                        System.out.println("Make sure you input all relevant information. Try Again.");
                        updateManager(projectToUpdate, updateAspect);

                    }

                    insertAll.setString(1, newManagerName);
                    insertAll.setString(2, projectToUpdate.manager.getPhone());
                    insertAll.setString(3, projectToUpdate.manager.getEmail());
                    insertAll.setString(4, projectToUpdate.manager.getAddress());
                    insertAll.executeUpdate();

                    updateProjectInfo.setString(1, newManagerName);
                    updateProjectInfo.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                    updateProjectInfo.executeUpdate();

                    deleteItem.setString(1, originalName);
                    deleteItem.executeUpdate();

                    projectToUpdate.manager.setName(newManagerName);

                }
                // The manager's phone is updated after being validated as a phone number starting with a 0 or +.
                case "phone" -> {

                    System.out.println(ENTER_COMMAND + MANAGER_STRING + PERSON_PHONE);
                    String newManagerPhone = personInfoChoice.nextLine();

                    newManagerPhone = validatePhoneNum(newManagerPhone, MANAGER_STRING);

                    updatePhone.setString(1, newManagerPhone);
                    updatePhone.setString(2, projectToUpdate.manager.getName());
                    updatePhone.executeUpdate();

                    projectToUpdate.manager.setPhone(newManagerPhone);

                }
                // The project manager's email is updated.
                case "email" -> {

                    System.out.println(ENTER_COMMAND + MANAGER_STRING + PERSON_EMAIL);
                    String newManagerEmail = personInfoChoice.nextLine();

                    updateEmail.setString(1, newManagerEmail);
                    updateEmail.setString(2, projectToUpdate.manager.getName());
                    updateEmail.executeUpdate();

                    projectToUpdate.manager.setEmail(newManagerEmail);

                }
                // The project manager's address is updated.
                case "address" -> {

                    System.out.println(ENTER_COMMAND + MANAGER_STRING + PERSON_ADDRESS);
                    String newManagerAddress = personInfoChoice.nextLine();

                    updateAddress.setString(1, newManagerAddress);
                    updateAddress.setString(2, projectToUpdate.manager.getName());
                    updateAddress.executeUpdate();

                    projectToUpdate.manager.setAddress(newManagerAddress);

                }
                // In this case, the previous method is called.
                case "back" -> getUpdateOptions(projectToUpdate);
                default -> {

                    System.out.println("You have input invalid information. Try Again.");
                    getUpdateOptions(projectToUpdate);

                }
            }
        } catch(Exception e){
            System.out.println("Invalid input. Try again.");
        }
        // The project is printed out with the updated information.
        System.out.println(projectToUpdate);
    }

    /**
     * The finalise() method updates the pay_complete table to make the project finalised and to set the complete
     * date to today's date. It then prints out an invoice for the customer and displays the amount they owe.
     * If the customer has paid for the project, an invoice is not printed.
     * @param projectToUpdate the project that will be updated.
     */
    private static void finalise(Project projectToUpdate) {

        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            PreparedStatement statement = connection.prepareStatement("UPDATE pay_complete SET finalised = 'Y', " +
                    "complete_date = ? WHERE proj_num = ?")){

            // If the project is already finalised, then this message will be printed.
            if(projectToUpdate.finalise){

                System.out.println("The project has already been finalised.");

            }
            // If the project has not been finalised, then it will be finalised.
            else {

                // The project's finalise attribute is set to true and the complete date is set to the current date.
                projectToUpdate.setFinalise(true);

                statement.setString(1, projectToUpdate.projectInfo.getCompleteDate());
                statement.setInt(2, projectToUpdate.projectInfo.getProjectNumber());
                statement.executeUpdate();

            }

            // If the total paid is equal to the total fee, then an invoice is created and printed.
            if (projectToUpdate.projectInfo.getTotalPaid() != projectToUpdate.projectInfo.getTotalFee()) {

                System.out.println(projectToUpdate.createInvoice());

            }
            // If the total fee is equal to the total paid, then this message is displayed.
            else {
                System.out.println("The customer has already settled their account.");
            }

        }catch(Exception e){
            System.out.println("Cannot connect to the database.");
        }
    }

    /**
     * The deleteProject() required confirmation from the user before deleting the project from the relevant tables.
     * @param projectToDelete the project that will be deleted.
     * @return a boolean is returned. True is returned with the user deletes the project. False when the user does
     * not delete the project.
     */
    private static boolean deleteProject(Project projectToDelete){

        // A try-with-resources block uses prepared statements to delete the related rows from all the tables.
        try(Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASS);
            PreparedStatement delPayComp = connection.prepareStatement("DELETE FROM pay_complete WHERE proj_num = ?");
            PreparedStatement delBuildInfo = connection.prepareStatement("DELETE FROM build_info WHERE erf_num = ?");
            PreparedStatement delArchitect = connection.prepareStatement("DELETE FROM architect WHERE arch_name = ?");
            PreparedStatement delContractor = connection.prepareStatement("DELETE FROM contractor WHERE cont_name = ?");
            PreparedStatement delCustomer = connection.prepareStatement("DELETE FROM customer WHERE cust_name = ?");
            PreparedStatement delEngineer = connection.prepareStatement("DELETE FROM engineer WHERE engi_name = ?");
            PreparedStatement delManager = connection.prepareStatement("DELETE FROM project_manager WHERE pm_name = ?")){

            Scanner toDelete = new Scanner(System.in);

            // The user is asked for confirmation of deletion.
            System.out.println("Are you sure you want to delete this project? y/n");
            String deleteConfirmation = toDelete.nextLine();

            // If the user inputs 'y', then the relevant rows are deleted.
            if(deleteConfirmation.equalsIgnoreCase("y")){

                delPayComp.setInt(1, projectToDelete.projectInfo.getProjectNumber());
                delPayComp.executeUpdate();

                delBuildInfo.setInt(1, projectToDelete.projectInfo.getErfNumber());
                delBuildInfo.executeUpdate();

                delArchitect.setString(1, projectToDelete.architect.getName());
                delArchitect.executeUpdate();

                delContractor.setString(1, projectToDelete.contractor.getName());
                delContractor.executeUpdate();

                delCustomer.setString(1, projectToDelete.customer.getName());
                delCustomer.executeUpdate();

                delEngineer.setString(1, projectToDelete.engineer.getName());
                delEngineer.executeUpdate();

                delManager.setString(1, projectToDelete.manager.getName());
                delManager.executeUpdate();

                System.out.println("Project deleted.");
                // True is returned.
                return true;

            }

        }catch(Exception e){
            System.out.println("Could not delete project.");
        }
        // If the if statement inside the try-with-resources block does not run, then false is returned.
        return false;

    }

    /**
     * The createProjectObjects() method gets information about projects(s) from a ResultSet, creates Project object(s),
     * and adds them to an array list that is then returned.
     * @param projectsToCreate the ResultSet that contains row(s) of project information.
     * @return an array list of the project(s) is returned.
     */
    private static ArrayList<Project> createProjectObjects(ResultSet projectsToCreate) throws SQLException {

        ArrayList<Project> listOfProjects = new ArrayList<>();

        while(projectsToCreate.next()){

            int projectNumber = projectsToCreate.getInt("project_info.proj_num");
            String projectName = projectsToCreate.getString("proj_name");
            String buildingType = projectsToCreate.getString("build_type");
            String buildingAddress = projectsToCreate.getString("build_address");
            int erfNumber = projectsToCreate.getInt("erf_num");
            double totalFee = projectsToCreate.getDouble("total_fee");
            double totalPaid = projectsToCreate.getDouble("total_paid");

            // In order to get the correct format for the LocalDate object, the formatDate() method is called.
            String deadline = projectsToCreate.getString("deadline");
            LocalDate deadlineDate = formatDate(deadline);
            // The complete date of the project is captured and will be set below once the Project object is created.
            Date completeDate = projectsToCreate.getDate("complete_date");

            // A ProjectInfo object is created passing some captured information above.
            ProjectInfo capturedProjectInfo = new ProjectInfo(projectName, buildingType, buildingAddress, erfNumber,
                    totalFee, deadlineDate);
            // The project number and total paid is then set after the ProjectInfo object is created.
            capturedProjectInfo.setProjectNumber(projectNumber);
            capturedProjectInfo.setTotalPaid(totalPaid);

            // Architect's information is stored and then a Person object is created.
            String architectName = projectsToCreate.getString("arch_name");
            String architectPhone = projectsToCreate.getString("arch_tele");
            String architectEmail = projectsToCreate.getString("arch_email");
            String architectAddress = projectsToCreate.getString("arch_address");

            Person capturedArchitect = new Person(Person.Type.ARCHITECT, architectName, architectPhone,
                    architectEmail, architectAddress);

            // Contractor's information is and then a Person object is created.
            String contractorName = projectsToCreate.getString("cont_name");
            String contractorPhone = projectsToCreate.getString("cont_tele");
            String contractorEmail = projectsToCreate.getString("cont_email");
            String contractorAddress = projectsToCreate.getString("cont_address");

            Person capturedContractor = new Person(Person.Type.CONTRACTOR, contractorName, contractorPhone,
                    contractorEmail, contractorAddress);

            // Customer's information is stored and then a Person object is created.
            String customerName = projectsToCreate.getString("cust_name");
            String customerPhone = projectsToCreate.getString("cust_tele");
            String customerEmail = projectsToCreate.getString("cust_email");
            String customerAddress = projectsToCreate.getString("cust_address");

            Person capturedCustomer = new Person(Person.Type.CUSTOMER, customerName, customerPhone,
                    customerEmail, customerAddress);

            // Engineer's information is stored and then a Person object is created.
            String engineerName = projectsToCreate.getString("engi_name");
            String engineerPhone = projectsToCreate.getString("engi_tele");
            String engineerEmail = projectsToCreate.getString("engi_email");
            String engineerAddress = projectsToCreate.getString("engi_address");

            Person capturedEngineer = new Person(Person.Type.ENGINEER, engineerName, engineerPhone,
                    engineerEmail, engineerAddress);

            // Manager's information is stored and then a Person object is created.
            String managerName = projectsToCreate.getString("pm_name");
            String managerPhone = projectsToCreate.getString("pm_tele");
            String managerEmail = projectsToCreate.getString("pm_email");
            String managerAddress = projectsToCreate.getString("pm_address");

            Person capturedManager = new Person(Person.Type.MANAGER, managerName, managerPhone,
                    managerEmail, managerAddress);

            // A Project object is created that passes the above objects.
            Project capturedProject = new Project(capturedProjectInfo, capturedArchitect, capturedContractor,
                    capturedCustomer, capturedEngineer, capturedManager);

            // If the completeDate is not null, then the project is set to finalised and the complete date is set.
            if(completeDate != null){

                capturedProject.finalise = true;
                LocalDate completeLocalDate = completeDate.toLocalDate();
                capturedProject.projectInfo.setCompleteDate(completeLocalDate);

            }
            // The project is added to the array created before the while statement.
            listOfProjects.add(capturedProject);
        }
        // The array is returned.
        return listOfProjects;
    }

    /**
     * The formatDate() method takes a string written date (which must follow the pattern yyyy-mm-dd),
     * formats it and parses it into a LocalDate.
     * @param stringDate string of the date to be converted to LocalDate.
     * @return a LocalDate is returned.
     */
    public static LocalDate formatDate(String stringDate){

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return LocalDate.parse(stringDate, dateFormatter);

    }

    /**
     * The isNumeric() method takes a string and uses a try-catch block to check if it is numerical or not.
     * @param string string to be checked.
     * @return a boolean is returned.
     */
    private static boolean isNumeric(String string) {

        // If the string is not null or empty, then this statement is printed and false is returned.
        if(string == null || string.equals("")) {
            System.out.println("No input detected.");
            return false;
        }

        // The try-catch block runs a parseInt function on the string and if it does not create an error,
        // then the string must be numeric and true is returned.
        try {
            int intValue = Integer.parseInt(string);
            return true;
        }
        // If a NumberFormatException is caught, then false is returned.
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * The validatePhoneNum() method checks whether an inputted string is a valid number. It does this by checking the
     * first character of the string. If the character does not equal 0 or +, then a while loop runs until the input's
     * first character equals one of these characters.
     * @param newPersonPhone string to be checked.
     * @param personString the type of person whose number is being checked.
     */
    private static String validatePhoneNum(String newPersonPhone, String personString) {

        Scanner personPhoneInfo = new Scanner(System.in);

        while(newPersonPhone.charAt(0) != '0' && newPersonPhone.charAt(0) != '+'){

            System.out.println("Not a valid phone number, try again.");
            System.out.println(ENTER_COMMAND + personString + PERSON_PHONE);
            newPersonPhone = personPhoneInfo.nextLine();

        }
        return newPersonPhone;
    }
}
