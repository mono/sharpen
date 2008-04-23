package sharpen.core.csharp.ast;

public class CSEvent extends CSMetaMember {
	
	private CSBlock _addBlock;
	private CSBlock _removeBlock;
	
	public CSEvent(String name, CSTypeReference type) {
		super(name, type);
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
	
	public void setAddBlock(CSBlock block) {
		_addBlock = block;
	}
	
	public void setRemoveBlock(CSBlock block) {
		_removeBlock = block;
	}

	public CSBlock getAddBlock() {
		return _addBlock;
	}

	public CSBlock getRemoveBlock() {
		return _removeBlock;
	}
}
