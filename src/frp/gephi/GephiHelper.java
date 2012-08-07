package frp.gephi;

import java.io.File;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GephiHelper {

	public DirectedGraph loadGraphFile(String topologyFileName) throws Exception{
		//Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file       
        File file = new File(topologyFileName);
        Container container = importController.importFile(file);
        container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        return graphModel.getDirectedGraph();
	}
}
