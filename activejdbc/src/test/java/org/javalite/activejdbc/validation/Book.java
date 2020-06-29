package org.javalite.activejdbc.validation;

public class Book extends ValidationSupport {

    private String title, isbn, authorFirstName, authorLastName;


    public Book() {
        validatePresenceOf("title", "authorFirstName");
    }


    //*************  Below code is needed  for tests only, ActiveWeb will set attributes
    // directly.



    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthorFirstName(String authorFirstName) {
        this.authorFirstName = authorFirstName;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", authorFirstName='" + authorFirstName + '\'' +
                ", authorLastName='" + authorLastName + '\'' +
                '}';
    }
}
