import {Injectable} from '@angular/core';
import {PluginUserRO} from "./pluginuser";

/**
 * @author Soumya
 * @since 5.0
 */
@Injectable()
export class PluginUserValidatorService {

  validatePluginUsers(users: PluginUserRO[]) {
    this.checkUserNameDuplicates(users);
  }

  private checkUserNameDuplicates(allUsers: PluginUserRO[]) {
    let seen = new Object();
    //allUsers = allUsers.filter(user => user.userName === UserState[UserState.PERSISTED]);
    allUsers.filter(user => {
      seen[user.userName] = seen[user.userName] == null ? 1 : seen[user.userName] + 1;
    });
    const list = Object.keys(seen).filter(key => seen[key] > 1);
    if (list.length > 0) {
      throw new Error('Duplicate plugin user name for users: ' + list.join(', '));
    }
  }
}
