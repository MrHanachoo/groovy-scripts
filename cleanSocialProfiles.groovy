import org.crsh.cli.*;
import org.crsh.text.ui.UIBuilder;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import org.exoplatform.services.jcr.impl.core.value.StringValue
import org.exoplatform.container.component.RequestLifeCycle
import org.exoplatform.services.organization.User

@Usage("Cleanup user social profiles and set void-deleted to true for already deleted users")
class cleanSocialProfiles {
  private rootContainer;
  private portalContainer;
  private orgService;
  private userHandler;

  private repoService;
  private session;
  private rootNode;
  private socOrgNode;
  private socOrgPath;
  private userNode;
  private userName;
  private userProfileNode;
  private isDeleted;
  private voidDeleted;
  private usersList;

  
  cleanSocialProfiles() {
    rootContainer = org.exoplatform.container.RootContainer.instance;
    portalContainer = rootContainer.getPortalContainer("portal");
    orgService = portalContainer.getComponentInstanceOfType(org.exoplatform.services.organization.OrganizationService.class);
    userHandler = orgService.userHandler;

    repoService = portalContainer.getComponentInstanceOfType(org.exoplatform.services.jcr.RepositoryService.class);
    session = repoService.getRepository("repository").getSystemSession("social");
    socOrgPath = "production/soc:providers/soc:organization/";
  }

  @Usage("get the list of deleted users whom still appear in People Directory") 
  @Command public void main(@Usage("- Cleanup user social profiles and set void-deleted to true for already deleted users") @Argument String perform) {
    rootNode = session.getRootNode();
    socOrgNode = rootNode.getNode(socOrgPath);
    allUsersNodes = socOrgNode.getNodes();
    def usersList = [];
    assert usersList.size() == 0;         
    assert usersList instanceof java.util.List;

    while(allUsersNodes.hasNext()) {
      userNode = allUsersNodes.nextNode();
      userName = userNode.getProperty("soc:remoteId").getValue().getString();
      //println userHandler.findUserByName(userName);
      isDeleted = userNode.getProperty("soc:isDeleted").getValue().getString();
      //formatNode(builder, userNode);
      if(isDeleted) {
        userProfileNode = userNode.getNode("soc:profile");
        if(userProfileNode.hasProperty("void-deleted")) {
          //javax.jcr.Value[]
          voidDeleted =  (userProfileNode.getProperty("void-deleted").getValues())[0].getString();
          if (voidDeleted == "false") {
            usersList.add(userName);
            try{
              RequestLifeCycle.begin(portalContainer)
              User user = userHandler.findUserByName(userName)
            if(!user) {
              if(perform != "perform"){
               println "## "+userName+" needs to get cleaned"
               } else {
               Value[] values = [new StringValue("true")]
               userProfileNode.getProperty("void-deleted").setValue(values)
               userProfileNode.save()
               println "## "+userName+" profile was cleaned ..."
             } 
            } else {
              println "## Problem when looking for "+userName
            }
          }
          catch(Exception e){

          }
          finally{
            RequestLifeCycle.end()
          }
          }
        } 
       
      }
    }
    println "SIZE:"+ usersList.size()+ "\nLIST: "+usersList+"\n";

  }

    
}