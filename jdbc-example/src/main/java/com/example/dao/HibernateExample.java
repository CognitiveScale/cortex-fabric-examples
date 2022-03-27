package com.example.dao;

import com.c12e.fabric.ConfigurationProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HibernateExample implements Example {

    private SessionFactory factory = null;

    @Override
    public void setup(ConfigurationProvider.JdbcConnectionParams connectionParams) {
        Map<String, String> settings = new HashMap<>();
        {
            settings.put("connection.driver_class", connectionParams.getDriverClassname());
            settings.put("hibernate.connection.url", connectionParams.getConnectionProtocol());
            settings.put("hibernate.dialect_resolvers", StandardDialectResolver.class.getName());
            connectionParams.getConnectionProperties().forEach((k, v) -> settings.put("hibernate.connection."+k, (String) v));

//            Setup tables for mapped entity classes to execute queries
            settings.put("hibernate.current_session_context_class", "thread");
            settings.put("hibernate.show_sql", "true");
            settings.put("hibernate.hbm2ddl.auto", "update");
            settings.put("hibernate.format_sql", "true");
        }

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings).build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addAnnotatedClass(Employee.class);
        Metadata metadata = metadataSources.buildMetadata();

        factory =  metadata.getSessionFactoryBuilder().build();
    }

    @Override
    public JsonNode execute(String hql) throws SQLException, IOException {
        // insert one record to populate some data
        addEmployee("James", "A", 1000);
        // list inserted records
        List<Employee> values = listEmployees(hql);
        return convertToJSON(values);
    }

    @Entity(name = "Employee")
    public static class Employee implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;
        private String firstName;
        private String lastName;
        private Integer salary;

        public Employee() {}
        public Employee(String fname, String lname, int salary) {
            this.firstName = fname;
            this.lastName = lname;
            this.salary = salary;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", salary=" + salary +
                    '}';
        }
    }

    public Integer addEmployee(String fname, String lname, int salary) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            Employee employee = new Employee(fname, lname, salary);
            Integer employeeID = (Integer) session.save(employee);
            tx.commit();
            return employeeID;
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
    }

    public List<Employee> listEmployees(String hql) {
        Transaction tx = null;
        try (Session session = factory.openSession()){
            tx = session.beginTransaction();
            List<Employee> employees = session.createQuery(hql).list();
            tx.commit();
            return employees;
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
