#! encoding:utf8
import email
import imaplib
import json
import os

class EmailUnseenConductor(object):
    def __init__(self, host, user, password):
        self.emails = []
        try:
            self.imapObj = imaplib.IMAP4(host=host)
            self.imapObj.login(user=user,password=password)
            # select INBOX
            self.imapObj.select('INBOX', False)
            #_, data = self.imapObj.search(None, 'UNSEEN')
            #for index in data[0].split():
            while(True):
                _, data = self.imapObj.search(None,'UNSEEN')
                if len(data) <= 0 or len(data[0]) <= 0:
                    break;
                index = data[0].split()[0]
                _, message_data = self.imapObj.fetch(index, '(RFC822)')
                self.emails.append(email.message_from_bytes(message_data[0][1]))
                self.imapObj.store(index,'+FLAGS',r'(\Deleted)')
            self.imapObj.expunge()
        finally:
            self.imapObj.close()
            self.imapObj.logout()

    def parseEmail(self,body_dir):
        for semail in self.emails:
            context_path = os.path.join(body_dir, 'context.txt')
            with open(context_path,'a+') as fps:
                fps.write('subject: ' + semail.get('subject') + '\n')
                fps.write('from: ' + semail.get('from') + '\n')
                fps.write('to: ' + semail.get('to') + '\n')
                fps.write('date: ' + semail.get('date') + '\n')
                for par in semail.walk():
                    attach_name = par.get_filename()
                    if attach_name:
                        fps.write('attachfile: ' + attach_name + '\n')
                        attach_dir = os.path.join(body_dir, 'attach')
                        os.makedirs(attach_dir)
                        file_path = os.path.join(attach_dir, attach_name)
                        file_data = par.get_payload(decode=True)
                        with open(file_path,'wb+') as fps:
                            fps.write(file_data)
                    else:
                        if par.get_payload(decode=True):
                            fps.write('context: ' + bytes.decode(par.get_payload(decode=True)) + '\n')


#test
if __name__ == '__main__':
    ec = EmailUnseenConductor(host='imap.163.com',user='user7458',password='Aa719588417')
    ec.parseEmail(body_dir='/home/lulu')