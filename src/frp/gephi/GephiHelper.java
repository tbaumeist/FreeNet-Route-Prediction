package frp.gephi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GephiHelper {

    public DirectedGraph loadGraphFile(String topologyFileName)
            throws Exception {
        File file = new File(topologyFileName);
        FileInputStream input = new FileInputStream(file);
        return loadGraphFile(input, getExtension(file));
    }

    public DirectedGraph loadGraphFile(InputStream topologyFile, String fileExt)
            throws Exception {
        // Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(
                ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        GraphModel graphModel = Lookup.getDefault()
                .lookup(GraphController.class).getModel();
        ImportController importController = Lookup.getDefault().lookup(
                ImportController.class);
        FileImporterBuilder importBuilder = getMatchingImporter(fileExt);
        if (importBuilder == null)
            throw new Exception(
                    "Unable to find file importer for the given file type : "
                            + fileExt);

        // Import file
        Container container = importController.importFile(topologyFile,
                importBuilder.buildImporter());
        container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED); // Force
                                                                    // DIRECTED

        // Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        // See if graph is well imported
        return graphModel.getDirectedGraph();
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    private FileImporterBuilder getMatchingImporter(String extension) {
        if (!extension.startsWith("."))
            extension = "." + extension;
        FileImporterBuilder[] fileImporterBuilders = Lookup.getDefault()
                .lookupAll(FileImporterBuilder.class)
                .toArray(new FileImporterBuilder[0]);
        for (FileImporterBuilder im : fileImporterBuilders) {
            for (FileType ft : im.getFileTypes()) {
                for (String ext : ft.getExtensions()) {
                    if (ext.equalsIgnoreCase(extension)) {
                        return im;
                    }
                }
            }
        }
        return null;
    }
}
