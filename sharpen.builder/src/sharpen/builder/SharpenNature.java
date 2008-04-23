package sharpen.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class SharpenNature implements IProjectNature {

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = Activator.PLUGIN_ID + ".sharpenNature";

	private IProject _project;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		IProjectDescription desc = _project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		if (containsBuilderCommand(commands)) return;

		desc.setBuildSpec(append(commands, newBuilderCommand(desc)));
		_project.setDescription(desc, null);
	}

	private boolean containsBuilderCommand(ICommand[] commands) {
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(SharpenBuilder.BUILDER_ID)) {
				return true;
			}
		}
		return false;
	}

	private ICommand newBuilderCommand(IProjectDescription desc) {
		ICommand command = desc.newCommand();
		command.setBuilderName(SharpenBuilder.BUILDER_ID);
		return command;
	}

	private ICommand[] append(ICommand[] commands, ICommand command) {
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);		
		newCommands[newCommands.length - 1] = command;
		return newCommands;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(SharpenBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return _project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this._project = project;
	}

}
