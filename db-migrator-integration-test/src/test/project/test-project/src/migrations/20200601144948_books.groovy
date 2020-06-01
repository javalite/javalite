
import org.javalite.activejdbc.Base

Base.findAll("select * from books").each {println "The book is: ${it.title}"}



