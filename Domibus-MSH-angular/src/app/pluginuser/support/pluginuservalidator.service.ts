import {Injectable} from '@angular/core';
import {PluginUserRO} from "./pluginuser";

/**
 * @author Soumya
 * @since 5.0
 */
@Injectable()
export class PluginUserValidatorService {

  validatePluginUsers(users: PluginUserRO[]) {
    this.checkPluginUserNameDuplication(users);
  }

  private checkPluginUserNameDuplication(allPluginUsers: PluginUserRO[]) {
    let uniqValues = []
    let dupValues = []
    for (let user of allPluginUsers) {
      if (uniqValues.indexOf(user.userName) != -1) {
        dupValues.push(user.userName)
      } else {
        uniqValues.push(user.userName)
      }
    }
    if (dupValues.length > 0) {
      throw new Error('Duplicate user name for plugin users: ' + dupValues);
    }
  }
}
