package org.dawnsci.macro.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.macro.MacroServiceImpl;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;
import org.eclipse.dawnsci.macro.api.IMacroService;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneratorTest {

	
	private static IMacroService mservice;
    
	@BeforeClass
	public static void setup() {
		mservice = new MacroServiceImpl();
	}
	
	@Test
	public void testUnnamedDataset() throws Exception {
		
		final IDataset nameless = Random.rand(new int[]{112, 112});
		
		AbstractMacroGenerator gen = mservice.getGenerator(IDataset.class);
		String line = gen.getPythonCommand(nameless);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		System.out.println(line);
	}
	
	@Test
	public void testUnnamedMap() throws Exception {
		
		final IDataset nameless = Random.rand(new int[]{112, 112});
		final Map<String, Object> map = new HashMap<String, Object>(1);
		map.put(null, nameless);
		AbstractMacroGenerator gen = mservice.getGenerator(Map.class);
		
		String line = gen.getPythonCommand(map);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		System.out.println(line);
	}
	
	@Test
	public void testUnnamed2ItemMap() throws Exception {
		
		final IDataset nameless = Random.rand(new int[]{112, 112});
		final Map<String, Object> map = new HashMap<String, Object>(1);
		map.put(null, nameless);
		
		final IDataset fred = Random.rand(new int[]{112, 112});
		fred.setName("Something else");
		map.put("fred", fred); // Should still be named fred.
		
		
		AbstractMacroGenerator gen = mservice.getGenerator(Map.class);
		
		String line = gen.getPythonCommand(map);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		if (!line.contains("data = ")) throw new Exception("Collection not assigned!");
		if (!"Something else".equals(fred.getName())) throw new Exception("Name clobbered!");
		System.out.println(line);
	}

	
	@Test
	public void testUnnamedList() throws Exception {
		
		final IDataset nameless = Random.rand(new int[]{112, 112});
		final List<Object> list = new ArrayList<Object>(1);
		list.add(nameless);
		AbstractMacroGenerator gen = mservice.getGenerator(List.class);
		
		String line = gen.getPythonCommand(list);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		System.out.println(line);
	}
	
	@Test
	public void testUnnamed2ItemList() throws Exception {
		
		final IDataset nameless = Random.rand(new int[]{112, 112});
		final List<Object> list = new ArrayList<Object>(1);
		list.add(nameless);
		
		final IDataset fred = Random.rand(new int[]{112, 112});
		fred.setName("fred");
		list.add(fred); // Should still be named fred.
		
		
		AbstractMacroGenerator gen = mservice.getGenerator(List.class);
		
		String line = gen.getPythonCommand(list);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		if (!line.contains("data = ")) throw new Exception("Collection not assigned!");
		System.out.println(line);
		
	}

	@Test
	public void testUnnamedRegion() throws Exception {
		
		IROI box = new RectangularROI(100, 200, Math.PI/2d);
		
		AbstractMacroGenerator gen = mservice.getGenerator(IROI.class);

		String line = gen.getPythonCommand(box);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		System.out.println(line);

	}

	@Test
	public void testUnnamedRegionList() throws Exception {
		
		IROI box = new RectangularROI(100, 200, Math.PI/2d);
		final List<Object> list = new ArrayList<Object>(1);
		list.add(box);
	
		AbstractMacroGenerator gen = mservice.getGenerator(List.class);

		String line = gen.getPythonCommand(list);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		System.out.println(line);

	}

	@Test
	public void testUnnamedRegionSeveralItemList() throws Exception {
		
		final List<Object> list = new ArrayList<Object>(1);

		IROI roi = new RectangularROI(100, 200, Math.PI/2d);
		list.add(roi);
		
		roi = new SectorROI(100, 200);
		list.add(roi);
		
		roi = new EllipticalROI(100, 0, 0);
		list.add(roi);

	
		AbstractMacroGenerator gen = mservice.getGenerator(List.class);

		String line = gen.getPythonCommand(list);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		if (!line.contains("data = ")) throw new Exception("Collection not assigned!");
		System.out.println(line);

	}
	
	@Test
	public void testUnnamedRegionSeveralItemMap() throws Exception {
		
		final Map<String, Object> map = new HashMap<String, Object>(1);

		IROI roi = new RectangularROI(100, 200, Math.PI/2d);
		map.put("box", roi);
		
		IROI sector = new SectorROI(100, 200);
		sector.setName("Something else");
		map.put("sector", sector);
		
		roi = new EllipticalROI(100, 0, 0);
		roi.setName("ellipse");
		map.put("ellipse", roi);

	
		AbstractMacroGenerator gen = mservice.getGenerator(Map.class);

		String line = gen.getPythonCommand(map);
		if (line==null) throw new Exception("Incorrect macro line generated!");
		if (line.trim().startsWith("=")) throw new Exception("No name generated!");
		if (!line.contains("data = ")) throw new Exception("Collection not assigned!");
		if (!"Something else".equals(sector.getName())) throw new Exception("Name clobbered!");
	
		System.out.println(line);

	}


}
