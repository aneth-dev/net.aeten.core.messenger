package net.aeten.core.messenger.test;

/**
 *
 * @author Thomas Pérennou
 */
public enum Acknowledge {

	SOLLICITED,
	SOLLICITED_NEED_ACKNOWLEDGE,
	UNSOLLICITED,
	UNSOLLICITED_NEED_ACKNOWLEDGE,
	OK,
	INVALID_MESSAGE,
	INVALID_DATA
}
