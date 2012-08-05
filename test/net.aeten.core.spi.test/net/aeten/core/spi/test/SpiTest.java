package net.aeten.core.spi.test;

import net.aeten.core.Format;
import net.aeten.core.spi.*;

public interface SpiTest {

	public void print();

	@Provider(SpiTest.class)
	public static class SpiTestImpl implements SpiTest {

		@FieldInit(required = true)
		private final String hello;

		@FieldInit()
		private final String name;

		protected SpiTestImpl(@SpiInitializer SpiTestInitializer init) {
			hello = init.getHello();
			name = init.hasName() ? init.getName() : "Toto";
		}

		@Override
		public void print() {
			System.out.println(hello + " " + name + "!");
		}

		@Configuration(name = "test.properties", provider = SpiTestImpl.class)
		public static void main(String[] args) {
			System.out.println("Hello");
			for (SpiTest test : Service.getProviders(SpiTest.class)) {
				test.print();
			}
		}
	}
}
