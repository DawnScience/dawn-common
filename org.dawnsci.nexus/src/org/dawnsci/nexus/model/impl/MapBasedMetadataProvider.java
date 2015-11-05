package org.dawnsci.nexus.model.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.model.api.NexusMetadataProvider;

public class MapBasedMetadataProvider implements NexusMetadataProvider {
	
	private final Map<String, Object> metadataMap = new HashMap<>();
	
	private NexusBaseClass category = null;
	
	public MapBasedMetadataProvider() {
		// do nothing
	}
	
	public MapBasedMetadataProvider(Map<String, ?> map) {
		metadataMap.putAll(map);
	}
	
	public void addMetadata(String name, Object value) {
		metadataMap.put(name, value);
	}
	
	public void setCategory(NexusBaseClass category) {
		this.category = category;
	}

	@Override
	public NexusBaseClass getCategory() {
		return category;
	}

	@Override
	public Iterator<MetadataEntry> getMetadataEntries() {
		return new MapBasedMetadataEntryIterator();
	}
	
	private class MapBasedMetadataEntryIterator implements Iterator<NexusMetadataProvider.MetadataEntry> {
		
		private final Iterator<Map.Entry<String, Object>> mapEntryIterator;
		
		public MapBasedMetadataEntryIterator() {
			mapEntryIterator = metadataMap.entrySet().iterator();
		}
		
		@Override
		public boolean hasNext() {
			return mapEntryIterator.hasNext();
		}

		@Override
		public MetadataEntry next() {
			return new MapEntryBasedMetadataEntry(mapEntryIterator.next());
		}
		
	}
	
	private static class MapEntryBasedMetadataEntry implements MetadataEntry {
		
		private final Map.Entry<String, Object> entry;
		
		private MapEntryBasedMetadataEntry(Map.Entry<String, Object> entry) {
			this.entry = entry;
		}

		@Override
		public String getName() {
			return entry.getKey();
		}

		@Override
		public Object getValue() {
			return entry.getValue();
		}
		
	}
	

}
