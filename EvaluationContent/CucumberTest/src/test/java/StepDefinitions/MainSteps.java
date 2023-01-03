package StepDefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Main;

public class MainSteps {
    @Given("a main methode")
    public void a_main_methode() {
        System.out.println("Given a main methode");
    }
    @When("you run main")
    public void you_run_main() {
        System.out.println("When the main methode is run");
    }
    @Then("console output is {string}")
    public void console_output_is(String string) {
        // Write code here that turns the phrase above into concrete actions
        System.out.println("Console Output is " + string);
    }
}
