/**
 * This class creates an object that contains information about a person attached to the project.
 */
public class Person {

    /**
     * enum Type is used to declare the constant attributes of Architect, Contractor, and Customer.
     */
    enum Type {
        ARCHITECT, CONTRACTOR, CUSTOMER, ENGINEER, MANAGER

    }
    // Attributes for the Person superclass are declared. All of them are strings.
    Type personType;
    String name;
    String phone;
    String email;
    String address;

    /**
     * The constructor for the Person class passes information about the person and sets the attributes.
     * @param personType The type of person working on the project - i.e. the architect, contractor, or customer
     * @param name The name of the person
     * @param phone The phone number of the person
     * @param email The email of the person
     * @param address The address of the person
     */
    Person(Type personType, String name, String phone, String email, String address){

        this.personType = personType;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;

    }

    /**
     * Sets the name of the person.
     * @param newName a new name for the person
     */
    public void setName(String newName){
        name = newName;
    }

    /**
     * Sets the telephone number of the person.
     * @param newPhone a new telephone number for the person
     */
    public void setPhone(String newPhone){
        phone = newPhone;
    }

    /**
     * Sets the email of the person.
     * @param newEmail a new email for the person
     */
    public void setEmail(String newEmail){
        email = newEmail;
    }

    /**
     * Sets the address of the person.
     * @param newAddress a new address for the person
     */
    public void setAddress(String newAddress){
        address = newAddress;
    }

    public String getName(){
        return name;
    }

    public String getPhone(){
        return phone;
    }

    public String getEmail(){
        return email;
    }

    public String getAddress(){
        return address;
    }

    /**
     * This method turns the Type attributes into relevant strings.
     * @return returns the string of the type of person
     */
    public String getPersonTypeString() {

        if(personType == Type.ARCHITECT){
            return "Architect";
        } else if (personType == Type.CONTRACTOR){
            return "Contractor";
        } else if (personType == Type.CUSTOMER){
            return "Customer";
        } else if (personType == Type.ENGINEER){
            return "Structural Engineer";
        } else if (personType == Type.MANAGER){
            return "Project Manager";
        } else{
            return null;
        }
    }

    /**
     * @return The toString() method returns a string with all the currently formatted information in an easy-to-read way.
     */
    public String toString() {
        String output = "\n" + getPersonTypeString();
        output += "\nName: " + name;
        output += "\nTelephone number: " + phone;
        output += "\nEmail: " + email;
        output += "\nAddress: " + address + "\n";

        return output;
    }


}


