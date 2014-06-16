package net.scapeemulator.game.model.definition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import net.scapeemulator.cache.Archive;
import net.scapeemulator.cache.Cache;
import net.scapeemulator.cache.Container;
import net.scapeemulator.cache.ReferenceTable;
import net.scapeemulator.cache.def.ItemDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ItemDefinitions {

	private static final Logger logger = LoggerFactory.getLogger(ItemDefinitions.class);
	private static ItemDefinition[] definitions;

	public static void init(Cache cache) throws IOException {
		int count = 0;

		Container tableContainer = Container.decode(cache.getStore().read(255, 19));
		ReferenceTable table = ReferenceTable.decode(tableContainer.getData());

		int files = table.capacity();
		definitions = new ItemDefinition[files * 256];

		for (int file = 0; file < files; file++) {
			ReferenceTable.Entry entry = table.getEntry(file);
			if (entry == null)
				continue;

			Archive archive = Archive.decode(cache.read(19, file).getData(), entry.size());
			int nonSparseMember = 0;
			for (int member = 0; member < entry.capacity(); member++) {
				ReferenceTable.ChildEntry childEntry = entry.getEntry(member);
				if (childEntry == null)
					continue;

				int id = file * 256 + member;
				ItemDefinition definition = ItemDefinition.decode(id, archive.getEntry(nonSparseMember++));
				definitions[id] = definition;
				count++;
			}
		}

		logger.info("Loaded " + count + " item definitions.");
		BufferedWriter bw = new BufferedWriter(new FileWriter("530 Item List.txt"));
		for(int i = 0; i < definitions.length; i++) {
			ItemDefinition d = definitions[i];
			if(d != null) {
				bw.write(i + " - " + d.getName() + " val: " + d.getValue() + "; stackable/noted?/notedId/unnotedId: " + d.isStackable() + 
						"/" + d.isNoted() + "/" + d.getNotedId() + "/" + d.getUnnotedId());
			} else {
				bw.write(i + " NO DATA");
			}
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}

	public static int count() {
		return definitions.length;
	}

	public static ItemDefinition forId(int id) {
		return definitions[id];
	}

}