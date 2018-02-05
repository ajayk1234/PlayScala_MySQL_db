package controllers
import javax.inject.Inject

import play.api.db._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.I18nSupport
import sun.security.util.Password

case class User(username: String, password: String)
case class UserData(firstname : String, lastname: String, email:String,username:String,password: String )

class HomeController @Inject()(db: Database,
                               val controllerComponents: ControllerComponents)
    extends BaseController
    with I18nSupport {
//  mapping of userdetails class

  val userDataForm: Form[UserData] = Form(
    mapping(
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText,
      "email" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserData.apply)(UserData.unapply)
  )

//  mapping of user class
  val userForm: Form[User] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  def home = Action {
    Ok(views.html.home())
  }

//  render user form which take input as firstname and lastname
  def addUser = Action { implicit request =>
    Ok(views.html.user(userDataForm))

  }

//  add values of User object to user table and shows message
  def addUserToDB = Action { implicit request =>
    val user = userDataForm.bindFromRequest.get //get user object which has input values
    val conn = db.getConnection() // conn object create connection which MySQL DB

    try {
      val prestmt = conn.prepareStatement("insert into user values(?,?,?,?,?)")
      prestmt.setString(1, user.firstname)
      prestmt.setString(2, user.lastname)
      prestmt.setString(3, user.email)
      prestmt.setString(4, user.username)
      prestmt.setString(5, user.password)
      prestmt.executeUpdate()
    } finally {
      conn.close()
    }

    Ok("User Added Successfully")
  }

//  fetch all the users from database and store it in map and call user html page which show all users
  def allUser = Action {
    var users: Map[String, String] = Map()
    val conn = db.getConnection()

    try {

      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT * from user ")
      while (rs.next()) {
        var firstname = rs.getString("username")
        var lastname = rs.getString("password")
        users += firstname -> lastname
      }
    } finally {
      conn.close()
    }
    Ok(views.html.index(users))
  }

  def login = Action{ implicit request =>
    Ok(views.html.login(userForm))
  }

  def doLogin = Action{ implicit request =>
    val user = userForm.bindFromRequest.get //get user object which has input values
    val conn = db.getConnection() // conn object create connection which MySQL DB
    var msg = ""

    try {
      val prestmt = conn.prepareStatement("select * from user where username = ? and password = ?")
      prestmt.setString(1, user.username)
      prestmt.setString(2, user.password)
      val rs = prestmt.executeQuery()
      if(rs.next()) {
        var firstname = rs.getString("username")
        var lastname = rs.getString("password")
        msg = "Login successfull"
      }
      else{
        msg= "Unauthorized"
      }

    } finally {
      conn.close()
    }

    Ok(msg)
  }


}
