(ns puppetlabs.puppetdb.meta.version
  "Versioning Utility Library

   This namespace contains some utility functions relating to checking version
   numbers of various fun things."
  (:require [clojure.java.jdbc :as sql]
            [clojure.string :as string]
            [clj-http.client :as client]
            [ring.util.codec :as ring-codec]
            [puppetlabs.puppetdb.cheshire :as json]
            [puppetlabs.dujour.version-check :as version-check]
            [puppetlabs.puppetdb.scf.storage-utils :as sutils]))

;; ### PuppetDB current version

(defn version
  "Get the version number of this PuppetDB installation."
  []
  {:post [(string? %)]}
  (version-check/get-version-string "puppetdb" "puppetlabs"))

;; ### Utility functions for checking for the latest available version of PuppetDB

(defn pdb-version-check-values*
  [db]
  (sql/with-connection db
    {:product-name {:group-id "puppetlabs"
                    :artifact-id "puppetdb"}
     :database-name (sutils/sql-current-connection-database-name)
     :database-version (string/join "." (sutils/sql-current-connection-database-version))}))

(def pdb-version-check-values
  (memoize pdb-version-check-values*))

(defn update-info
  "Make a request to the puppetlabs server to determine the latest available
  version of PuppetDB.  Returns the JSON object received from the server, which
  is expected to be a map containing keys `:version`, `:newer`, and `:link`.

  Returns `nil` if the request does not succeed for some reason."
  [update-server db]
  (version-check/update-info (pdb-version-check-values db) update-server))

(defn check-for-updates!
  [update-server db]
  (version-check/check-for-updates! (pdb-version-check-values db) update-server))
