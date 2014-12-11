package org.dawnsci.macro.generator;

class ClassKey {
	private Class<? extends Object> cls;

	public ClassKey(Class<? extends Object> cls) {
		this.cls=cls;
	}

	@Override
	public int hashCode() {
		// All in same bucket on purpose.
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassKey other = (ClassKey) obj;
		if (cls == null) {
			if (other.cls != null)
				return false;
		} else {
			if (cls.isAssignableFrom(other.cls) || 
					other.cls.isAssignableFrom(cls)) {
				return true;
			}
		}
		
		return false;
	}

}