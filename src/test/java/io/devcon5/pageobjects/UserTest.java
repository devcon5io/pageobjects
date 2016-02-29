package io.devcon5.pageobjects;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 */
public class UserTest {

    /**
     * The class under test
     */
    private User subject = new User("user", "password");


    @Test
    public void testGetUsername() throws Exception {
        assertEquals("user", subject.getUsername());
    }

    @Test
    public void testGetPassword() throws Exception {
        assertEquals("password", subject.getPassword());

    }
}
