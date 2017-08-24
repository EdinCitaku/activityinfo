package org.activityinfo.test.ui;

import org.activityinfo.test.sut.UserAccount;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests that users can log in
 *
 */
public class LoginTest {

    @Rule
    public UiTestHarness harness = new UiTestHarness();

    /**
     *
     * Scenario: Successful login
     * Given that the user "auto.qa@bedatadriven.com" is signed up
     * When I login as "auto.qa@bedatadriven.com" with my correct password
     * Then my dashboard should open
     */
    @Test
    public void successful() {

        UserAccount account = harness.createAccount();
        System.out.println(account.getEmail());
        System.out.println(account.getPassword());

        harness.getLoginPage().navigateTo().loginAs(account).andExpectSuccess();
    }
    @Test
    public void failed()  {

        UserAccount badAccount = new UserAccount("bademail@example.com","1234");

        harness.getLoginPage().navigateTo().loginAs(badAccount).assertErrorMessageIsVisible();
    }

}
