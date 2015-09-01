package org.activityinfo.test.pageobject.web.entry;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import cucumber.api.DataTable;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.test.driver.BsDataEntryDriver;
import org.activityinfo.test.driver.DataEntryDriver;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.pageobject.api.FluentElement;
import org.activityinfo.test.pageobject.api.FluentElements;
import org.activityinfo.test.pageobject.gxt.GxtGrid;
import org.activityinfo.test.pageobject.gxt.GxtModal;
import org.activityinfo.test.pageobject.gxt.GxtPanel;
import org.activityinfo.test.pageobject.gxt.GxtTree;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.activityinfo.test.pageobject.api.XPathBuilder.withClass;
import static org.activityinfo.test.pageobject.api.XPathBuilder.withText;


public class DataEntryTab {

    private final GxtTree formTree;
    private final FluentElement container;

    public DataEntryTab(FluentElement container) {
        this.container = container;
        this.formTree = GxtPanel.find(container, I18N.CONSTANTS.activities()).tree();

        // on "Data Entry" tab selection first activity is always selected by default
        // we have to manually select it to let test framework know "real selection" (e.g. for Details tab)
        this.formTree.waitUntilLoaded();
        Optional<GxtTree.GxtNode> formNode = formTree.firstRootNode();
        if (formNode.isPresent()) {
            formNode.get().select();
        }

    }
    
    public DataEntryTab navigateToForm(String formNameOrDatabaseName) {
        formTree.waitUntilLoaded();
        Optional<GxtTree.GxtNode> formNode = formTree.search(formNameOrDatabaseName);
        if(!formNode.isPresent()) {
            throw new AssertionError(String.format("Form '%s' is not present in data entry tree", formNameOrDatabaseName));
        }
        formNode.get().select();
        
        return this;
    }

    public DataEntryFilter filter(String labelOfFilter) {
        FluentElement element = container.find().descendants().span(withText(labelOfFilter)).ancestor().div(withClass("x-panel")).first();
        return new DataEntryFilter(element, labelOfFilter);
    }
    
    public DataEntryDriver newSubmission() {
        buttonClick(I18N.CONSTANTS.newSite());
        final FluentElement windowElement = container.root();
        return container.waitFor(new Function<WebDriver, DataEntryDriver>() {
            @Override
            public DataEntryDriver apply(WebDriver input) {
                if(windowElement.find().div(withClass(GxtModal.CLASS_NAME)).exists()) {
                    return new GxtDataEntryDriver(new GxtModal(windowElement));
                } else if(windowElement.find().div(withClass("formPanel")).firstIfPresent().isPresent()) {
                    return new BsDataEntryDriver(windowElement);
                } else {
                    return null;
                }
            }
        });
    }
    
    public DataEntryDriver updateSubmission() {
        buttonClick(I18N.CONSTANTS.edit());
        return new GxtFormDataEntryDriver(new GxtModal(container));
    }

    public DataEntryTab buttonClick(String buttonLabel) {
        final FluentElement button = container.find().button(withText(buttonLabel)).first();
        button.waitUntil(new Predicate<WebDriver>() { // wait until button become enabled (there may be small period before it becomes enabled)
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return button.attribute("aria-disabled").equals("false");
            }
        });
        button.click();
        return this;
    }
    
    public int getCurrentSiteCount() {
        return container.waitFor(new Function<WebDriver, Integer>() {
            @Override
            public Integer apply(WebDriver input) {
                Optional<FluentElement> countLabel = container.find()
                        .div(withClass("my-paging-display"))
                        .firstIfPresent();
                
                if(countLabel.isPresent()) {
                    String text = countLabel.get().text();
                    if (text.equals("No data to display")) {
                        return 0;
                    }

                    Matcher matcher = Pattern.compile("Displaying ([\\d,]+) - ([\\d+,]+) of ([\\d,]+)").matcher(text);
                    if (matcher.matches()) {
                        return Integer.parseInt(matcher.group(3).replace(",", ""));
                    }
                }
                return null;
            }
        });
    }

    public File export() {
        container.find().button(withText("Export")).clickWhenReady();
    
        String link = container.waitFor(new Function<WebDriver, String>() {
            @Nullable
            @Override
            public String apply(WebDriver input) {
                WebElement link = input.findElement(By.partialLinkText("Click here if your download does not start"));
                return link.getAttribute("href");
            }
        });

        URL url;
        try {
            url = new URL(link);
        } catch (MalformedURLException e) {
            throw new AssertionError("Bad link: " + link);
        }

        try {
            File file = File.createTempFile("export", ".xls");
            ByteStreams.copy(Resources.asByteSource(url), Files.asByteSink(file));
            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void selectSubmission(int rowIndex) {
        GxtGrid grid = GxtGrid.waitForGrids(container).first().get();
        grid.waitUntilAtLeastOneRowIsLoaded();
        try {
            grid.rows().get(rowIndex).select();
        } catch (StaleElementReferenceException e) {
            GxtGrid.waitForGrids(container).first().get().rows().get(rowIndex).select();
        }
    }
    
    public void selectTab(String tabName) {
        FluentElement tab = container.find().span(withClass("x-tab-strip-text"), withText(tabName)).first();
        if(!tab.find().ancestor().li(withClass("x-tab-strip-active")).exists()) {
            tab.click();
        }
    }

    public List<HistoryEntry> changes() {

        selectTab("History");

        return container.waitFor(new Function<WebDriver, List<HistoryEntry>>() {
            @Override
            public List<HistoryEntry> apply(WebDriver input) {
                try {
                    List<HistoryEntry> entries = Lists.newArrayList();
                    FluentElements paragraphs = container.find().div(withClass("details")).p().span().asList();
                    for (FluentElement p : paragraphs) {
                        String text;
                        text = p.text();

                        if(text.contains("Loading")) {
                            return null;
                        }
                        if(!text.trim().isEmpty()) {
                            HistoryEntry entry = new HistoryEntry(text);
                            FluentElements changes = p.find().parent().p().followingSibling().ul().li().asList();
                            for (FluentElement change : changes) {
                                entry.addChange(change.text());
                            }

                            entries.add(entry);
                        }
                    }
                    return entries;
                } catch (StaleElementReferenceException ignored) {
                    return null;
                }
            }
        });
    }

    public DetailsEntry details() {
        selectTab("Details");
        try {
            Thread.sleep(300); // sometimes it's too fast and we read details of previous row, give it time to switch
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        container.waitFor(By.className("indicatorHeading"));
        return container.waitFor(new Function<WebDriver, DetailsEntry>() {
            @Override
            public DetailsEntry apply(WebDriver input) {
                DetailsEntry detailsEntry = new DetailsEntry();

                FluentElement detailsPanel = container.find().div(withClass("details")).first();

                FluentElements indicatorNames = detailsPanel.find().td(withClass("indicatorHeading")).asList();
                FluentElements indicatorValues = detailsPanel.find().td(withClass("indicatorValue")).asList();
                FluentElements indicatorUnits = detailsPanel.find().td(withClass("indicatorUnits")).asList();

                Preconditions.checkState(indicatorNames.size() == indicatorValues.size(),
                        "Number of field names and values do not match on Details tab. Names: " + indicatorNames.size() +
                                ", values: " + indicatorValues.size());

                for (int i = 0; i < indicatorNames.size(); i++) {
                    String name = indicatorNames.get(i).text();
                    String value = indicatorValues.get(i).text();
                    //String unit = indicatorUnits.get(i).text(); // skip units, we want to handle also text indicators here

                    detailsEntry.getFieldValues().add(new FieldValue(name, value));
                }

                FluentElements attributeGroupNames = detailsPanel.find().p(withClass("attribute")).span(withClass("groupName")).asList();
                FluentElements attributeValues = detailsPanel.find().p(withClass("attribute")).span(withClass("attValues")).asList();

                Preconditions.checkState(attributeGroupNames.size() == attributeValues.size());

                for (int i = 0; i < attributeGroupNames.size(); i++) {
                    String name = attributeGroupNames.get(i).text();
                    String value = attributeValues.get(i).text();

                    if (name.endsWith(":")) {
                        name = name.substring(0, name.length() - 1);
                    }

                    FieldValue fieldValue = new FieldValue(name, value).
                            setType(Optional.of(EnumType.TYPE_CLASS));
                    detailsEntry.getFieldValues().add(fieldValue);
                }

                Optional<FluentElement> commentValue = detailsPanel.find().p(withClass("comments")).span(withClass("attValues")).firstIfPresent();
                if (commentValue.isPresent()) {
                    detailsEntry.getFieldValues().add(new FieldValue("Comments", commentValue.get().text()));
                }

                return detailsEntry;
            }
        });
    }

    public FluentElement getContainer() {
        return container;
    }

    public void importData(DataTable dataTable) {
        container.find().button(withText(I18N.CONSTANTS.importText())).clickWhenReady();

        //BsModal importDialog = BsModal.find(container);
        //importDialog.form()
    }
}
