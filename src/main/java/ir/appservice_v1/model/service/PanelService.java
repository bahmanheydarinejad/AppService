package ir.appservice_v1.model.service;

import ir.appservice_v1.model.entity.application.ui.Menu;
import ir.appservice_v1.model.entity.application.ui.Panel;
import ir.appservice_v1.model.repository.MenuRepository;
import ir.appservice_v1.model.repository.PanelRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ApplicationScope
public class PanelService extends CrudService<Panel> {

    private MenuRepository menuRepository;

    public PanelService(PanelRepository panelRepository, MenuRepository menuRepository) {
        super(panelRepository, Panel.class);
        this.menuRepository = menuRepository;
    }

    public boolean existsByDisplayNameIgnoreCase(String displayName) {
        return crudRepository.existsByDisplayNameIgnoreCase(displayName);
    }

    public Panel addWithMenu(Panel panel, int priority, String icon, Menu parent) {
        Menu menu = new Menu();
        if (panel.getMenu() == null) {
            menu.setType(Menu.PANEL_MENU);
            menu.setPriority(priority);
            menu.setDisplayName(panel.getDisplayName());
            menu.setIcon(icon);
            menu.setParent(parent);
            menuRepository.save(menu);
            panel.setMenu(menu);
        }
        return getPanelRepository().save(panel);
    }

    private PanelRepository getPanelRepository() {
        return (PanelRepository) crudRepository;
    }

    public Panel getDefaultPanel() {
        return getPanelRepository().findByDisplayNameIgnoreCase("Home");
    }

    public Map<String, File> getPanelsResources() {

        List<File> results = new ArrayList<>();
        for (File file : new File("./src/main/resources/META-INF/resources/").listFiles()) {
            fileSearch(file, results);
        }

        Map<String, File> panelsResources = new HashMap<>();
        results.forEach(file -> panelsResources.put(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("resources")).replace("resources\\", ""), file));
        return panelsResources;
    }

    private void fileSearch(File file, List<File> result) {
        if (file.isFile() && file.getName().endsWith(".xhtml")) {
            result.add(file);
        } else if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                fileSearch(f, result);
            }
        }
    }
}
