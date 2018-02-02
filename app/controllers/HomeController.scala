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

//  render user form which take input as firstname and lastname
  def addUser = Action { implicit request =>
    Ok(views.html.user(userForm))

  }

//  add values of User object to user table and shows message
  def addUserToDB = Action { implicit request =>
    val user = userForm.bindFromRequest.get //get user object which has input values
    val conn = db.getConnection() // conn object create connection which MySQL DB

    try {
      val prestmt = conn.prepareStatement("insert into user values(?,?)")
      prestmt.setString(1, user.firstname)
      prestmt.setString(2, user.lastname)
      prestmt.executeUpdate()
    } finally {
      conn.close()
    }

    Ok("User added successfully")
  }

//  fetch all the users from database and store it in map and call user html page which show all users
  def allUser = Action {
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
    Ok(views.html.user(users))
  }

}
