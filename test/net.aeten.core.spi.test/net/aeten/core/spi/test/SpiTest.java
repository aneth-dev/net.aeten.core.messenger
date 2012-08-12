package net.aeten.core.spi.test;

import java.util.HashMap;
import java.util.Map;
import net.aeten.core.Identifiable;
import net.aeten.core.spi.*;

public interface SpiTest {

	public void print();

	public static class Foo implements Identifiable {

		@FieldInit()
		private final String id;

		public Foo(@SpiInitializer FooInitializer init) {
			this.id = init.getId();
		}

		@Override
		public String getIdentifier() {
			return id;
		}
	}

	@Provider(SpiTest.class)
	public static class SpiTestImpl implements SpiTest {

		@FieldInit()
		private final String hello;
		@FieldInit()
		private final String name;
		@FieldInit(required = false)
		private final Map<String, Foo> foos;

		protected SpiTestImpl(@SpiInitializer SpiTestInitializer init) {
			hello = init.getHello();
			name = init.getName();
			foos = init.hasFoos() ? init.getFoos() : new HashMap<>();
		}

		@Override
		public void print() {
			System.out.println(hello + " " + name + "!");
			for (Map.Entry<String, Foo> foo : foos.entrySet()) {
				System.out.println(foo.getKey() + ": " + foo.getValue().getIdentifier());
			}
		}

		@Configuration(name = "test.yaml", provider = SpiTestImpl.class)
		public static void main(String[] args) {
			System.out.println("SPI Test");
			for (SpiTest test : Service.getProviders(SpiTest.class)) {
				test.print();
			}
		}
	}
}
