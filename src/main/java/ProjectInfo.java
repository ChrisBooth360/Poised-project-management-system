import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class creates an object that contains information about the project, excluding the people involved in the project.
 */
public class ProjectInfo {

    // Attributes for ProjectInfo are declared.
    int projectNumber;
    String projectName;
    String buildingType;
    String address;
    int erfNumber;
    double totalFee;
    LocalDate deadline;
    double totalPaid;
    double totalOwed;
    LocalDate completeDate;

    /**
     * The constructor for the ProjectInfo class takes in six parameters that are used to set the attributes.
     * The other four attributes are automatically set.
     * @param projectName The name of the project
     * @param buildingType The type of building that will be built
     * @param address The address of the building site
     * @param erfNumber The ERF Number of the building
     * @param totalFee The total fee of the projects
     * @param deadline The deadline of the project
     */
    ProjectInfo(String projectName, String buildingType, String address, int erfNumber, double totalFee,
                LocalDate deadline){

        this.projectNumber = 1;
        this.projectName = projectName;
        this.buildingType = buildingType;
        this.address = address;
        this.erfNumber = erfNumber;
        this.totalFee = totalFee;
        this.deadline = deadline;
        // this.totalPaid attribute is automatically set to 0.
        this.totalPaid = 0.00;
        this.totalOwed = totalFee - totalPaid;
        // this.completeDate is automatically set to null.
        this.completeDate = null;

    }

    /**
     * Sets the project number of the project
     * @param newProjectNumber new project number integer
     */
    public void setProjectNumber(int newProjectNumber){

        projectNumber = newProjectNumber;

    }

    /**
     * Sets the project name
     * @param newProjectName new project name string
     */
    public void setProjectName(String newProjectName){

        projectName = newProjectName;

    }

    /**
     * Sets the building type of the project
     * @param newBuildingType new building type string
     */
    public void setBuildingType(String newBuildingType){

        buildingType = newBuildingType;

    }

    /**
     * Sets the address of the project
     * @param newAddress new address of the project string
     */
    public void setAddress(String newAddress){

        address = newAddress;

    }

    /**
     * Sets the total fee of the project
     * @param newTotalFee new total fee double
     */
    public void setTotalFee(double newTotalFee){
        totalFee = newTotalFee;
    }

    /**
     * Sets the deadline of the project
     * @param newDeadline new LocalDate deadline of the project
     */
    public void setDeadline(LocalDate newDeadline){
        deadline = newDeadline;
    }

    /**
     * Sets the total paid of the project
     * @param newTotalPaid new total paid double
     */
    public void setTotalPaid(double newTotalPaid){
        totalPaid = newTotalPaid;
    }

    /**
     * Sets the completed date of the project
     * @param newCompleteDate new LocalDate of the complete date
     */
    public void setCompleteDate(LocalDate newCompleteDate){

        completeDate = newCompleteDate;

    }

    /**
     * Gets the project number
     * @return integer of the project number
     */
    public int getProjectNumber(){
        return projectNumber;
    }

    /**
     * Gets the project name
     * @return string of the project name
     */
    public String getProjectName(){
        return projectName;
    }

    /**
     * Gets the building type
     * @return string of the building type
     */
    public String getBuildingType(){
        return buildingType;
    }

    /**
     * Gets the building address
     * @return string of the building address
     */
    public String getAddress(){
        return address;
    }

    /**
     * Gets the ERF Number
     * @return integer of the ERF number
     */
    public int getErfNumber(){
        return erfNumber;
    }

    /**
     * Gets the total fee
     * @return double of the total fee
     */
    public double getTotalFee(){
        return totalFee;
    }

    /**
     * Gets the deadline
     * @return String of the deadline
     */
    public String getDeadline(){

        DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return deadline.format(datePattern);
    }

    /**
     * Gets the total paid
     * @return double of the total paid
     */
    public double getTotalPaid(){
        return totalPaid;
    }

    /**
     * Gets the total owed by subtracting the total paid from the total fee.
     * @return double of the total owed
     */
    public double getTotalOwed(){

        return totalFee - totalPaid;

    }

    /**
     * Gets the complete date. If complete date is null, then it is set to today's date. The complete date is formatted
     * into a string.
     * @return string of the complete date
     */
    public String getCompleteDate(){

        // If there is no complete date, then it is set to today's date.
        if(completeDate == null){

            /*
        The completeDate attribute is set to the current date, which is then formatted to a string.
        https://www.javatpoint.com/java-get-current-date
         */
            this.completeDate = LocalDate.now();

        }

        DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return completeDate.format(datePattern);


    }

    /**
     * @return The toString() method returns a string with all the currently formatted information in an easy-to-read way.
     */
    public String toString(){

        String output = "Project Number: " + getProjectNumber();
        output += "\nProject Name: " + getProjectName();
        output += "\nBuilding Type: " + getBuildingType();
        output += "\nBuilding Address: " + getAddress();
        output += "\nERF Number: " + getErfNumber();
        output += "\nTotal Fee: R" + String.format("%.2f", getTotalFee());
        output += "\nTotal Paid: R" + String.format("%.2f", getTotalPaid());
        output += "\nTotal Owed: R" + String.format("%.2f", getTotalOwed());
        output += "\nDeadline: " + getDeadline() + "\n";

        return output;

    }

}
