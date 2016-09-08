package io.toast.tk.maven.plugin.data;

import java.util.List;

public class ActionAdapterDescriptor {
	public final String name;

	public final List<ActionAdapterDescriptorLine> sentences;

	public ActionAdapterDescriptor(
		String name,
		List<ActionAdapterDescriptorLine> sentences) {
		this.name = name;
		this.sentences = sentences;
	}
}
