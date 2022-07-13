/**
 * This class creates Project objects that contain all information about each project.
 */
public class Project {

    // Attributes for the Project class are declared - the majority of which are objects from other classes.
    ProjectInfo projectInfo;
    Person architect;
    Person contractor;
    Person customer;
    Person engineer;
    Person manager;
    boolean finalise;

    /**
     * The constructor for the Project class passes information about a project - this includes four objects.
     * @param projectInfo An object from the ProjectInfo class
     * @param architect An object from the Person class with an Architect type
     * @param contractor An object from the Person class with a Contractor type
     * @param customer An object from the Person class with a Customer type
     */
    Project(ProjectInfo projectInfo, Person architect, Person contractor, Person customer, Person engineer,
            Person manager){

        this.projectInfo = projectInfo;
        this.architect = architect;
        this.contractor = contractor;
        this.customer = customer;
        this.engineer = engineer;
        this.manager = manager;

        // If the user chose not to input a project name, then this else if statement runs.
        if(projectInfo.projectName.equals("") && customer.name.contains(" ")){

            /*
            If the user inputs a blank project name AND the customer's name contains a space,
            then the customer's name is split in two and this.projectName is set to the building type plus the
            customer's last name.
             */
            String[] firstLastName = customer.name.split(" ", 2);

            projectInfo.setProjectName(projectInfo.buildingType + " " + firstLastName[1]);

        }
        /*
        If the user inputs a blank project name AND the customer's name does not contain a space,
        then this.projectName is set to the building type plus the customer's only input name.
        */
        else if(projectInfo.projectName.equals("") && !customer.name.contains(" ")){

            projectInfo.setProjectName(projectInfo.buildingType + " " + customer.name);

        }

        // this.finalise attribute is automatically set to false.
        this.finalise = false;

    }

    /**
     * Finalises the project
     * @param finalise boolean is passed of whether the project has been finalised or not.
     */
    public void setFinalise(boolean finalise) {
        this.finalise = finalise;
    }

    /**
     * This method is called when a project is finalised and an invoice needs to be created.
     * @return a string of the invoice is returned.
     */
    public String createInvoice(){

        // The customer's details, the complete date of the project and the total amount they owe are added to a string,
        // which is returned.
        String invoice = "\nCustomer Invoice\n" + customer;
        invoice += "\nComplete Date: " + projectInfo.getCompleteDate();
        invoice += "\nAmount owed: R" + String.format("%.2f", projectInfo.getTotalOwed()) + "\n";

        return invoice;
    }

    /**
     * @return The toString() method returns a string with all the currently formatted information in an easy-to-read way.
     */
    public String toString() {
        String output = projectInfo.toString();

        // If the project has been finalised, the complete date is displayed, otherwise Incomplete is displayed.
        if(finalise){

            output += "Date Complete: " + projectInfo.getCompleteDate() + "\n";

        } else {
            output += "Date Complete: Incomplete\n";
        }

        output += architect;
        output += contractor;
        output += customer;
        output += engineer;
        output += manager;

        return output;
    }
}
