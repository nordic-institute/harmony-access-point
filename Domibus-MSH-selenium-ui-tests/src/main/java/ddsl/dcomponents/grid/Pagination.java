package ddsl.dcomponents.grid;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DLink;
import ddsl.dobjects.DObject;
import ddsl.dobjects.Select;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class Pagination extends DComponent {
	
	
	@FindBy(css = "li.pages")
	List<WebElement> pgLinks;
	@FindBy(css = "li.pages.active")
	WebElement activePageLnk;
	@FindBy(css = "datatable-footer > div > datatable-pager > ul > li:nth-child(1)")
	WebElement skipFirstLnk;
	@FindBy(css = "datatable-footer > div > datatable-pager > ul > li:nth-last-child(1)")
	WebElement skipLastLnk;
	@FindBy(css = "datatable-footer > div > datatable-pager > ul > li:nth-last-child(2)")
	WebElement nextPageLnk;
	@FindBy(css = "datatable-footer > div > datatable-pager > ul > li:nth-child(2)")
	WebElement prevPageLnk;
	@FindBy(id = "pagesize_id")
	WebElement pageSizeSelectContainer;
	@FindBy(css = "datatable-footer > div > div.page-count")
	WebElement pageCount;
	
	public Pagination(WebDriver driver) {
		super(driver);
		
		log.debug("initiating pagination controls");
		PageFactory.initElements(driver, this);
		
	}
	
	public Select getPageSizeSelect() {
		return new Select(driver, pageSizeSelectContainer);
	}
	
	public boolean hasNextPage() {
		
		try {
			String attr = getNextPageLnk().getAttribute("class");
			return !(StringUtils.equalsIgnoreCase("disabled", attr));
		} catch (Exception e) {
		}
		return false;
	}
	
	public boolean hasPrevPage() {
		
		try {
			return !(StringUtils.equalsIgnoreCase("disabled", getPrevPageLnk().getAttribute("class")));
		} catch (Exception e) {
		}
		return false;
	}
	
	public int getExpectedNoOfPages() throws Exception {
		
		try {
			log.debug("getting expected number of pages");
			
			int noOfItems = getTotalItems();
			int itemsPerPg = Integer.valueOf(getPageSizeSelect().getSelectedValue());
			
			return (int) Math.ceil((double) noOfItems / itemsPerPg);
		} catch (NumberFormatException e) {
			log.error("EXCEPTION: ", e);
		}
		return -1;
	}
	
	public int getNoOfItemsOnLastPg() throws Exception {
		
		try {
			log.debug("getting expected number of items on last page");
			
			int noOfItems = getTotalItems();
			int itemsPerPg = Integer.valueOf(getPageSizeSelect().getSelectedValue());
			
			return noOfItems % itemsPerPg;
		} catch (NumberFormatException e) {
			log.error("EXCEPTION: ", e);
		}
		return -1;
	}
	
	public boolean isPaginationPresent() {
		log.debug("checking if pagination is present on page");
		return (getActivePageLnk().isPresent());
	}
	
	//	if pagination is not present we return 1 by default
	public Integer getActivePage() throws Exception {
		
		try {
			log.debug("getting active page number");
			
			if (!getActivePageLnk().isPresent()) {
				return 1;
			}
			return Integer.valueOf(getActivePageLnk().getLinkText());
		} catch (NumberFormatException e) {
		}
		return -1;
	}
	
	public void goToPage(int pgNo) throws Exception {
		
		log.debug("going to page .. " + pgNo);
		
		try {
			for (WebElement pgLink : pgLinks) {
				
				DLink pageLink = new DLink(driver, pgLink);
				if (Integer.valueOf(pageLink.getText()) == pgNo) {
					pageLink.click();
					PageFactory.initElements(driver, this);
					return;
				}
			}
		} catch (NumberFormatException e) {
			log.error("EXCEPTION: ", e);
		}
	}
	
	
	public void skipToFirstPage() {
		log.debug("skip to FIRST page of results");
		
		try {
//			weToDLink(pgLinks.get(0)).click();
			weToDLink(skipFirstLnk).click();
			wait.forAttributeToContain(pgLinks.get(0), "class", "active");
		} catch (Exception e) {
		}
		PageFactory.initElements(driver, this);
		
	}
	
	public void skipToLastPage() throws Exception {
		log.debug("skip to last page of results");
		getSkipLastLnk().click();
		PageFactory.initElements(driver, this);
	}
	
	public void goToNextPage() throws Exception {
		log.debug("going to next page");
		if (hasNextPage()) {
			getNextPageLnk().click();
		}
		PageFactory.initElements(driver, this);
	}
	
	public void goToPrevPage() throws Exception {
		log.debug("going to prev page");
		if (hasPrevPage()) {
			getPrevPageLnk().click();
		}
		PageFactory.initElements(driver, this);
	}
	
	
	public int getTotalItems() {
		
		try {
			log.debug("getting total number of items to be displayed");
			
			String raw = weToDobject(pageCount).getText();
			if (raw.contains("total")) {
				String[] splits = raw.split("/");
				for (String split : splits) {
					if (split.contains("total")) {
						String total = split.replaceAll("\\D", "");
						log.debug("Total number of records  " + Integer.valueOf(total));
						return Integer.valueOf(total);
					}
				}
			}
		} catch (NumberFormatException e) {
			log.error("EXCEPTION: ", e);
		} catch (Exception e) {
			log.error("EXCEPTION: ", e);
		}
		return 0;
	}
	
	public Integer getNoOfSelectedItems() {
		
		try {
			log.debug("getting number of selected items in grid");
			
			String raw = pageCount.getText().trim();
			if (raw.contains("selected")) {
				String[] splits = raw.split("/");
				for (String split : splits) {
					if (split.contains("selected")) {
						String selected = split.replaceAll("\\D", "");
						return Integer.valueOf(selected);
					}
				}
			}
		} catch (NumberFormatException e) {
			log.error("EXCEPTION: ", e);
		}
		return null;
	}
	
	public DLink getActivePageLnk() {
		return new DLink(driver, activePageLnk);
	}
	
	public DLink getSkipFirstLnk() {
		return new DLink(driver, skipFirstLnk);
	}
	
	public DLink getSkipLastLnk() {
		return new DLink(driver, skipLastLnk);
	}
	
	public DLink getNextPageLnk() {
		return new DLink(driver, nextPageLnk);
	}
	
	public DLink getPrevPageLnk() {
		return new DLink(driver, prevPageLnk);
	}
	
	public DObject getPageCount() {
		return new DObject(driver, pageCount);
	}
}
