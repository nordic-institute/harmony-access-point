package pages.domains;

import ddsl.dcomponents.grid.DGrid;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DomainsGrid extends DGrid {

    By activeSwitchStatus = By.cssSelector("input[role=switch]");
    By activeSwitch = By.cssSelector("mat-slide-toggle[id *= 'mat-slide-toggle']");

    public DomainsGrid(WebDriver driver) {
        super(driver);
    }

    @Override
    public HashMap<String, String> getRowInfo(int rowNumber) throws Exception {
        log.info("getting row info for row number " + rowNumber);

        HashMap<String, String> info = new HashMap<>();

        WebElement rowElement = getRowElement(rowNumber);
        List<WebElement> cells = rowElement.findElements(cellSelector);

        info.put("Domain Code", cells.get(0).getText());
        info.put("Domain Name", cells.get(1).getText());
        info.put("Active", cells.get(2).findElement(activeSwitchStatus).getAttribute("aria-checked"));

        return info;
    }

    @Override
    public ArrayList<HashMap<String, String>> getListedRowInfo() throws Exception {
        log.info("getting row info for all listed rows");
        ArrayList<HashMap<String, String>> info = new ArrayList<>();

        for (int i = 0; i < getRowsNo(); i++) {
            info.add(getRowInfo(i));
        }

        return info;
    }

    public void toggleActive(int rowNumber) throws Exception {
        WebElement rowElement = getRowElement(rowNumber);
        List<WebElement> cells = rowElement.findElements(cellSelector);
        cells.get(2).findElement(activeSwitch).click();

        waitForRowsToLoad();
    }

    public void toggleActive(String domainCode) throws Exception {
        int rowNumber = getIndexOf("Domain Code", domainCode);
        toggleActive(rowNumber);
    }

    public boolean isActive(String domainCode) throws Exception {
        int rowNumber = getIndexOf("Domain Code", domainCode);
        return getRowInfo(rowNumber).get("Active").equals("true");
    }


}
