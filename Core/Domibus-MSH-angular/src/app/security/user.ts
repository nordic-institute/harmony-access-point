export class User {
  id: number;
  username: string;
  authorities: Array<string>;
  defaultPasswordUsed: boolean;
  daysTillExpiration: number;
  externalAuthProvider: boolean;

  constructor (id: number,
               username: string,
               profile: string,
               authorities: Array<string>,
               defaultPasswordUsed: boolean,
               daysTillExpiration: number,
               externalAuthProvider: boolean) {
  }
}
