package com.chuwa.springboottesting.integration;

import com.chuwa.springboottesting.model.Employee;
import com.chuwa.springboottesting.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author b1go
 * @date 7/11/22 11:34 PM
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class EmployeeControllerIT {

    // default dbname: test, username: test, password: test
    @Container
    private static MySQLContainer mySQLContainer = new MySQLContainer("mysql:latest")
            .withUsername("chuwa")
            .withPassword("chuwa")
            .withDatabaseName("ems_dev");

    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @Resource
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Resource
    private ObjectMapper objectMapper;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employee = Employee.builder()
                .firstName("Chuwa")
                .lastName("America")
                .email("chuwa@gmail.com")
                .build();
    }

    @Test
    public void testCreateEmployee() throws Exception{

        System.out.println(mySQLContainer.getUsername());
        System.out.println(mySQLContainer.getPassword());
        System.out.println(mySQLContainer.getDatabaseName());
        System.out.println(mySQLContainer.getJdbcUrl());
        // given - precondition or setup


        // when - action or behaviour that we are going test
        ResultActions response = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)));

        // then - verify the result or output using assert statements
        response.andDo(print()).
                andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName",
                        is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName",
                        is(employee.getLastName())))
                .andExpect(jsonPath("$.email",
                        is(employee.getEmail())));

    }

    @Test
    public void testGetAllEmployees() throws Exception {

        //given - precondition or setup
        List<Employee> employees = new ArrayList<>();
        employees.add(employee);
        employees.add(Employee.builder().firstName("chuwa1").lastName("america1").email("chuwa1@gmail.com").build());
        employeeRepository.saveAll(employees);

        // when - action or the behaviour that we are going test
        ResultActions response = mockMvc.perform(get("/api/v1/employees"));

        // then - verify the output
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(employees.size())));
    }

    // positive scenario - valid employee id
    // JUnit test for GET employee by id REST API
    @Test
    public void testGetByEmployeeId() throws Exception {
        // given - precondition or setup
        Employee save = employeeRepository.save(employee);

        // when -  action or the behaviour that we are going test
        ResultActions response = mockMvc.perform(get("/api/v1/employees/{id}", save.getId()));

        // then - verify the output
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));

    }

    // negative scenario - valid employee id
    // JUnit test for GET employee by id REST API
    @Test
    public void testInvalidEmployeeId_ReturnEmpty() throws Exception {
        // given - precondition or setup
        Employee save = employeeRepository.save(employee);

        // when -  action or the behaviour that we are going test
        ResultActions response = mockMvc.perform(get("/api/v1/employees/{id}", save.getId() + 1));

        // then - verify the output
        response.andExpect(status().isNotFound())
                .andDo(print());
    }

    // JUnit test for update employee REST API - positive scenario
    @Test
    public void testUpdatedEmployee() throws Exception {
        // given - precondition or setup
        Employee save = employeeRepository.save(employee);
        Employee updatedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Snow")
                .email("John@gmail.com")
                .build();

        // when -  action or the behaviour that we are going test
        ResultActions response = mockMvc.perform(put("/api/v1/employees/{id}", save.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEmployee)));


        // then - verify the output
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.firstName", is(updatedEmployee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(updatedEmployee.getLastName())))
                .andExpect(jsonPath("$.email", is(updatedEmployee.getEmail())));
    }

    // JUnit test for update employee REST API - negative scenario
    @Test
    public void testUpdatedEmployee_Return404() throws Exception {
        // given - precondition or setup
        employeeRepository.save(employee);
        Employee updatedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Snow")
                .email("John@gmail.com")
                .build();

        // when -  action or the behaviour that we are going test
        ResultActions response = mockMvc.perform(put("/api/v1/employees/{id}", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEmployee)));

        // then - verify the output
        response.andExpect(status().isNotFound())
                .andDo(print());
    }

    // JUnit test for delete employee REST API
    @Test
    public void testDeleteByEmployeeId_Return200() throws Exception {
        // given - precondition or setup
        Employee save = employeeRepository.save(employee);

        // when -  action or the behaviour that we are going test
        ResultActions response = mockMvc.perform(delete("/api/v1/employees/{id}", save.getId()));

        // then - verify the output
        response.andExpect(status().isOk())
                .andDo(print());
    }
}
