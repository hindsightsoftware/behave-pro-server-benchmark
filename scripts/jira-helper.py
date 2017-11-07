import httplib2
from base64 import b64encode
import json
import string
import pickle

class Http:
    def __init__(self, method, uri):
        self.method = method
        self.uri = uri
        self.headers = {}
        self.h = httplib2.Http()
        self.h.follow_redirects = False
    
    def setUser(self, username, password):
        userAndPass = b64encode(username + ":" + password).decode("ascii")
        self.headers['Authorization'] = 'Basic %s' %  userAndPass
        return self

    def setHeader(self, key, value):
        self.headers[key] = value
        return self

    def throw(self, response, content):
        raise IOError(self.method + " " + self.uri + " returned " + str(response.status) + "\n" + content)
    
    def send(self):
        print("Sending " + self.method + " " + self.uri)
        (resp, content) = self.h.request(
            self.uri,
            self.method, 
            "",
            headers=self.headers
        )

        if resp.status >= 200 and resp.status < 400:
            return content
        else:
            self.throw(resp, content)

    def sendJson(self, data):
        print("Sending " + self.method + " " + self.uri)
        (resp, content) = self.h.request(
            self.uri,
            self.method, 
            json.dumps(data),
            headers=self.headers
        )

        if resp.status >= 200 and resp.status < 400:
            return content
        else:
            self.throw(resp, content)

#request = Http("POST", "localhost", 8080, "/rest/auth/1/session")
#request.setHeader("Content-Type", "application/json")
#response = request.sendJson({
#    "username": "admin",
#    "password": "admin"
#})
#
#cookie = response.getheader("set-cookie")

# Get all projects
def getProjects(baseurl):
    request = Http("GET", baseurl + "/rest/api/2/project")
    request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
    request.setUser("admin", "admin")
    response = request.send()
    projectIds = []
    data = json.loads(response)
    for project in data:
        projectIds.append(project['id'])
    return projectIds

def enableProject(baseurl, projectId):
    request = Http("GET", baseurl + "/rest/behavepro/2.0/project/" + projectId + "/admin/enabled")
    request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
    request.setHeader("Content-Type", "application/json")
    request.setUser("admin", "admin")
    request.sendJson({
        "enable": True
    })

# Get and merge users
def getUsers(baseurl):
    try:
        pclFile = open('users.pkl', 'rb')
        return pickle.load(pclFile)
    except IOError:
        users = {}
        for c in string.ascii_lowercase:
            request = Http("GET", baseurl + "/rest/api/latest/user/search?startAt=0&maxResults=1000&username=" + c)
            request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
            request.setUser("admin", "admin")
            response = request.send()
            data = json.loads(response)
            print("Got " + str(len(data)) + " users for letter " + c)
            for user in data:
                key = user['key']
                if key not in users and key != "admin" and key != "jdg-rest-admin" :
                    request = Http("GET", baseurl + "/rest/api/2/user?key=" + key + "&expand=groups,applicationRoles")
                    request.setUser("admin", "admin")
                    response = request.send()
                    jsonData = json.loads(response)
                    groups = jsonData['groups']['items']
                    roles = jsonData['applicationRoles']['items']
                    users[key] = {}
                    users[key]['email'] = user['emailAddress']
                    users[key]['fullname'] = user['displayName']
                    groupsFiltered = []
                    for group in groups:
                        groupsFiltered.append(group['name'])
                    rolesFiltered = []
                    for role in roles:
                        rolesFiltered.append(role['key'])
                    users[key]['groups'] = groupsFiltered
                    users[key]['roles'] = rolesFiltered

        output = open('users.pkl', 'wb')
        pickle.dump(users, output)
        return users

# Delete users
def deleteUsers(baseurl, users):
    for key in users:
        print("Deleting user " + key)
        request = Http("DELETE", baseurl + "/rest/api/2/user?key=" + key)
        request.setUser("admin", "admin")
        request.send()

# Create users
def createUsers(baseurl, users):
    for key in users:
        print("Creating user " + key)
        data = users[key]
        request = Http("POST", baseurl + "/rest/api/2/user")
        request.setHeader("Content-Type", "application/json")
        request.setUser("admin", "admin")
        request.sendJson({
            "name": key,
            "password": key,
            "emailAddress": data['email'],
            "displayName": data['fullname'],
            "applicationKeys": [
                "jira-core",
                "jira-software-users"
            ]
        })

        for group in data['groups']:
            print("Adding user " + key + " to group " + group)
            request = Http("POST", "http://localhost:8080/rest/api/2/group/user?groupname=" + group)
            request.setHeader("Content-Type", "application/json")
            request.setUser("admin", "admin")
            request.sendJson({
                "name": key
            })

def getActive(users):
    active = {}
    for key in users:
        user = users[key]
        if len(user['roles']) > 0:
            active[key] = user
    return active

def getIssues(baseurl, offset, total):
    request = Http("GET", baseurl + "/rest/api/2/search?jql=resolution+%3D+unresolved+ORDER+BY+priority+DESC%2C+created+ASC&sortBy=&startAt=" + str(offset) + "&maxResults=" + str(total))
    request.setHeader("Content-Type", "application/json")
    request.setUser("admin", "admin")
    request.setHeader("Accept", "*/*")
    response = request.send()
    return json.loads(response)['issues']

def assignIssue(baseurl, issue, user):
    request = Http("PUT", baseurl + "/rest/api/2/issue/" + issue + "/assignee")
    request.setHeader("Content-Type", "application/json")
    request.setUser("admin", "admin")
    request.setHeader("Accept", "*/*")
    response = request.sendJson({
        "name": user
    })

import sys

def main():
    if len(sys.argv) == 2:
        print("Missing args!")
        return

    baseurl = sys.argv[1]
    
    if sys.argv[2] == "fix":
        users = getUsers(baseurl)
        deleteUsers(baseurl, users)
        createUsers(baseurl, users)

    elif sys.argv[2] == "find":
        users = getUsers(baseurl)
        print(users[sys.argv[3]])

    elif sys.argv[2] == "active":
        users = getUsers(baseurl)
        active = getActive(users)
        file = open("users_with_roles.csv","w") 
        file.write("userKey,userPassword\n")
        for key in active:
            file.write(key + "," + key + "\n")

    elif sys.argv[2] == "assign":
        users = getUsers(baseurl)
        active = getActive(users)

        activeKeys = []
        for user in active:
            activeKeys.append(user)

        file = open("users_with_issues.csv","w") 
        file.write("userKey,issueKey\n")
        offset = 0

        for t in range(0, 100):
            issues = getIssues(baseurl, offset, len(activeKeys))
            issueKeys = []
            for issue in issues:
                issueKeys.append(issue['key'])

            for i in range(0, len(activeKeys)):
                assignIssue(baseurl, issueKeys[i], activeKeys[i])
                file.write(activeKeys[i] + "," + issueKeys[i] + "\n")
            offset = offset + len(activeKeys)

    elif sys.argv[2] == "dict":
        issues = getIssues(baseurl, 0, 200)
        file = open("issue_dictionary.csv","w") 
        file.write("searchToken,searchIssueKey\n")
        for issue in issues:
            key = issue['key']
            tokens = issue['fields']['summary'].split()
            for token in tokens:
                try:
                    file.write(token + "," + key + "\n")
                except UnicodeEncodeError:
                    continue

    elif sys.argv[2] == "enable":
        projects = getProjects(baseurl)
        for project in projects:
            enableProject(baseurl, project)
    
    else:
        raise RuntimeError("Unknown argument: " + sys.argv[2])

if __name__ == "__main__":
    main()
