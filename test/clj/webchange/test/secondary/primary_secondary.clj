(ns webchange.test.secondary.primary-secondary
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [webchange.test.fixtures.core :as f]
            [webchange.handler :as handler]
            [webchange.db.core :refer [*db*] :as db]
            [webchange.common.date-time :as dt]
            [mount.core :as mount]
            [clojure.data.json :as json]
            [webchange.secondary.core :as core]
            [clojure.tools.logging :as log]))

(use-fixtures :once f/init)
(use-fixtures :each f/clear-db-fixture f/with-default-school)

(deftest can-create-update-course
  (let [course-created (f/course-created)
        dataset (f/dataset-created {:course-id (:id course-created)} )
        dataset-item-created (f/dataset-item-created  {:dataset-id (:id dataset)})
        lesson-set-created (f/lesson-set-created {:dataset-id (:dataset-id dataset-item-created) :data {:items [{:id (:id dataset-item-created)}]}})
        scene-created (f/scene-created course-created)
        stat (core/get-course-update (str f/default-school-id))
        ]
      (let [
            school (:school stat)
            course (first (:courses stat))
            course-versions (first (:course-versions stat))
            dataset (first (:datasets stat))
            dataset-item (first (:dataset-items stat))
            lesson-set (first (:lesson-sets stat))
            scene (first (:scenes stat))
            scene-version (first (:scene-versions stat))
            ]
        (assert (= (:id school) f/default-school-id))
        (assert (= (:id course) (:course-id scene-created)))
        (assert (= (:id course-versions) (:version-id course-created)))
        (assert (= (:id dataset) (:dataset-id dataset-item)))
        (assert (= (:id dataset-item) (:id dataset-item-created)))
        (assert (= (:id lesson-set) (:id lesson-set-created)))
        (assert (= (:id scene) (:id scene-created)))
        (assert (= (:id scene-version) (:version-id scene-created)))
        )))

(deftest can-update-school
  (let  [name "NewName"
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                    (assoc stat :school {:id f/default-school-id :name name})
                    )
         ]
    (core/import-primary-data! update)
    (let [school (db/get-school {:id f/default-school-id})]
      (assert (= name (:name school)))
      )
    ))

(deftest can-update-course-and-version
  (let  [name "NewName"
         data {:initial-scene "helloKitty"}
         course-created (f/course-created)
         course-old (db/get-course {:slug (:slug course-created)})
         course-version-old (db/get-course-version {:id (:version-id course-created)})
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                      (assoc stat :courses [(-> course-old
                                               (assoc :name name))])
                      (assoc stat :course-versions [(-> course-version-old
                                                        (assoc :data data)
                                                        (assoc :created-at (dt/date-time2iso-str (:created-at course-version-old)))
                                                        )])
                      )
         ]
    (core/import-primary-data! update)
    (let [course-updated (db/get-course {:slug (:slug course-created)})
          course-version-updated (db/get-course-version {:id (:version-id course-created)})]
      (assert (= name (:name course-updated)))
      (assert (= data (:data course-version-updated)))
      )
    ))

(deftest can-update-dataset-and-item
  (let  [
         name "NewName"
         data {:initial-scene "helloKitty"}
         dataset-item-created (f/dataset-item-created)
         dataset-old (db/get-dataset {:id (:dataset-id dataset-item-created)})
         dataset-item-old (db/get-dataset-item {:id (:id dataset-item-created)})
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                      (assoc stat :datasets [(-> dataset-old
                                                (assoc :name name))])
                      (assoc stat :dataset-items  [(-> dataset-item-old
                                                      (assoc :data data)
                                                      )])
                      )
         ]
    (core/import-primary-data! update)
    (let [dataset-updated  (db/get-dataset {:id (:dataset-id dataset-item-created)})
          dataset-item-updated (db/get-dataset-item {:id (:id dataset-item-created)})]
      (assert (= name (:name dataset-updated)))
      (assert (= data (:data dataset-item-updated)))
      )
    ))

(deftest can-update-scenes-and-version
  (let  [
         name "NewName"
         data {:initial-scene "helloKitty"}
         scene-created (f/scene-created)
         scene-old (db/get-scene {:course_id (:course-id scene-created) :name (:name scene-created)})
         scene-version-old (db/get-scene-version {:id (:version-id scene-created)})
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                      (assoc stat :scenes [(-> scene-old
                                                 (assoc :name name))])
                      (assoc stat :scene-versions  [(-> scene-version-old
                                                       (assoc :data data)
                                                        (assoc :created-at (dt/date-time2iso-str (:created-at scene-version-old)))
                                                       )])
                      )
         ]
    (core/import-primary-data! update)
    (let [scene-updated (db/get-scene {:course_id (:course-id scene-created) :name name})
          scene-version-updated (db/get-scene-version {:id (:version-id scene-created)})]
      (assert (= name (:name scene-updated)))
      (assert (= data (:data scene-version-updated)))
      )
    ))

(deftest can-update-lesson-set
  (let  [
         name "NewName"
         data {:initial-scene "helloKitty"}
         lesson-set-created (f/lesson-set-created)
         lesson-set-old (db/get-lesson-set-by-name {:dataset_id (:dataset-id lesson-set-created) :name (:name lesson-set-created)})
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                      (assoc stat :lesson-sets [(-> lesson-set-old
                                               (assoc :name name))])
                      )
         ]
    (core/import-primary-data! update)
    (let [lesson-set-updated (db/get-lesson-set-by-name {:dataset_id (:dataset-id lesson-set-created) :name name})]
      (assert (= name (:name lesson-set-updated)))
      )
    ))

(deftest can-remove-course-and-version
  (let  [course-created (f/course-created)
         course-old (db/get-course {:slug (:slug course-created)})
         course-version-old (db/get-course-version {:id (:version-id course-created)})
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                      (assoc stat :courses [])
                      (assoc stat :course-versions []))
         ]
    (core/import-primary-data! update)
    (let [course-updated (db/get-course {:slug (:slug course-created)})
          course-version-updated (db/get-course-version {:id (:version-id course-created)})]
      (assert (= nil course-updated))
      (assert (= nil course-version-updated)) )))

(deftest can-remove-datasets-and-item
  (let  [dataset-item-created (f/dataset-item-created)
         dataset-old (db/get-dataset {:id (:dataset-id dataset-item-created)})
         dataset-item-old (db/get-dataset-item {:id (:id dataset-item-created)})
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                      (assoc stat :datasets [])
                      (assoc stat :dataset-items  [])
                      )
         ]
    (core/import-primary-data! update)
    (let [dataset-updated (db/get-dataset {:id (:dataset-id dataset-item-created)})
          dataset-item-updated (db/get-dataset-item {:id (:id dataset-item-created)})]
      (assert (= nil dataset-updated))
      (assert (= nil dataset-item-updated)) )))

(deftest can-remove-datasets-and-item
  (let  [scene-created (f/scene-created)
         scene-old (db/get-scene {:course_id (:course-id scene-created) :name (:name scene-created)})
         scene-version-old (db/get-scene-version {:id (:version-id scene-created)})
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                      (assoc stat :scenes [])
                      (assoc stat :scene-versions  []))
         ]
    (core/import-primary-data! update)
    (let [scene-updated (db/get-scene {:course_id (:course-id scene-created) :name (:name scene-created)})
          scene-version-updated (db/get-scene-version {:id (:version-id scene-created)})]
      (assert (= nil scene-updated))
      (assert (= nil scene-version-updated)) )))

(deftest can-remove-lesson
  (let  [lesson-set-created (f/lesson-set-created)
         lesson-set-old (db/get-lesson-set-by-name {:dataset_id (:dataset-id lesson-set-created) :name (:name lesson-set-created)})
         update (as-> (core/get-course-update (str f/default-school-id)) stat
                      (assoc stat :lesson-sets []))]
    (core/import-primary-data! update)
    (let [lesson-set-updated (db/get-lesson-set-by-name {:dataset_id (:dataset-id lesson-set-created) :name (:name lesson-set-created)})]
      (assert (= nil lesson-set-updated)) )))