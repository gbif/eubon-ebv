package org.gbif.eubon.ebv;

import org.gbif.eubon.ebv.data.DataDAO;
import org.gbif.eubon.ebv.resource.TileResource;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

/**
 * The main entry point for running the member node.
 */
public class TileServerApplication extends Application<TileServerConfiguration> {

  private static final String APPLICATION_NAME = "EUBON EBV Tile Server";

  public static void main(String[] args) throws Exception {
    new TileServerApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public final void initialize(Bootstrap<TileServerConfiguration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html", "assets"));
  }

  @Override
  public final void run(TileServerConfiguration configuration, Environment environment) {

    final DBIFactory factory = new DBIFactory();
    final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "MySQL");
    final DataDAO dataDao = jdbi.onDemand(DataDAO.class);
    environment.jersey().register(new TileResource(dataDao));
    //environment.jersey().setUrlPattern("/api/*");

  }
}
