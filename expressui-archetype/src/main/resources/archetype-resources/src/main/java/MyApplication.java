#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import ${package}.view.PersonPage;
import com.expressui.core.MainApplication;
import com.expressui.core.view.menu.MenuBarNode;

public class MyApplication extends MainApplication {

    @Override
    public void configureLeftMenuBar(MenuBarNode rootNode) {
        rootNode.addPage("People", PersonPage.class);
    }

    @Override
    public void configureRightMenuBar(MenuBarNode rootNode) {
    }

    @Override
    public String getCustomTheme() {
        return "custom";
    }

    @Override
    public void init() {
        super.init();

        securityService.loginAsDefaultSystemUser();
        displayPage(PersonPage.class);
        mainMenuBar.refresh();
    }
}
