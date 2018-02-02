package controllers
import javax.inject.Inject

import play.api.db._
import play.api.mvc._
import scala.collection.mutable.ListBuffer
import javax.inject._
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.I18nSupport
import views.html.helper.form

case class User(firstname: String, lastname: String)

class HomeController @Inject()(db: Database,
                               val controllerComponents: ControllerComponents)
    extends BaseController
    with I18nSupport {

//  mapping of user class
  val userForm: Form[User] = Form(
    mapping(
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  def addUserToDB = Action { implicit request =>
    val user = userForm.bindFromRequest.get
    val conn = db.getConnection()

    try{
      val prestmt = conn.prepareStatement("insert into user values(?,?)")
      prestmt.setString(1, user.firstname)
      prestmt.setString(2, user.lastname)
      prestmt.executeUpdate()
    }
    finally {
      conn.close()
    }

    Ok("User added successfully")
  }

  def addUser = Action { implicit request =>
    Ok(views.html.login(userForm))

  }

  def index = Action {
    var users: Map[String, String] = Map()
    val conn = db.getConnection()

    try {

      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT * from user ")
      while (rs.next()) {
        var firstname = rs.getString("firstname")
        var lastname = rs.getString("lastname")
        users += firstname -> lastname
      }
    } finally {
      conn.close()
    }
    Ok(views.html.index(users))
  }

}
