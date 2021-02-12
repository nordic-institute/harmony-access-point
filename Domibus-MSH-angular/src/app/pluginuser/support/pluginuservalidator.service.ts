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
    let uniq_values = []
    let dup_values = []
    for (let x of allPluginUsers) {
      if (uniq_values.indexOf(x.userName) != -1) {
        dup_values.push(x.userName)
      } else {
        uniq_values.push(x.userName)
      }
    }
    if (dup_values.length > 0) {
      throw new Error('Duplicate user name for plugin users: ' + dup_values);
    }
  }
}
