package com.chuwa.springboottesting.controller;


import com.chuwa.springboottesting.model.Employee;
import com.chuwa.springboottesting.service.EmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import javax.annotation.Resource;

/**
 * @author b1go
 * @date 7/10/22 11:47 PM
 */
@WebMvcTest
public class EmployeeControllerTest {

    /**
     * @Autowired can not find that kind of bean, so I use @Resource
     */
    @Resource
    private MockMvc mockMvc;

    /**
     * The @MockBean annotation tells Spring to create a mock instance of EmployeeService and add it to the
     * application context so that it's injected into EmployeeController.
     */
    @MockBean
    private EmployeeService employeeService;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    public void testCreateEmployee() throws Exception {

        //given - precondition or setup
        Employee employee = Employee.builder()
                .firstName("chuwa")
                .lastName("america")
                .email("chuwa@gmail.com")
                .build();
        BDDMockito.given(employeeService.saveEmployee(ArgumentMatchers.any(Employee.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        // when - action or the behaviour that we are going test
        ResultActions res = mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)));

        // then - verify the output
        res.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(employee.getFirstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(employee.getLastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(employee.getEmail())));
    }
}