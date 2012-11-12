package org.dawb.common.ui.tree;

import java.util.EventListener;

import javax.measure.quantity.Quantity;

public interface AmountListener<E extends Quantity> extends EventListener {

	void amountChanged(AmountEvent<E> evt);
}
