package resources
import org.flywaydb.core.Flyway

object FlywayMigration {
  def migrate(): Unit = {
    val flyway = Flyway.configure()
      .dataSource(
        "jdbc:postgresql://localhost:5435/pixelbattle", 
        "admin", 
        "password"
      )
      .load()
    
    flyway.migrate() 
    println("Миграции применены успешно!")
  }
}

