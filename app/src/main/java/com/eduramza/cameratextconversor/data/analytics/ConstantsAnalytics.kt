package com.eduramza.cameratextconversor.data.analytics

class ConstantsAnalytics {
    companion object {
        const val PARAM_AREA = "area"

        const val CONTENT_BUTTON = "button"
        const val CONTENT_ADMOB = "admob"
        const val CONTENT_EDIT = "edit"

        class Camera {
            companion object {
                const val SCREEN_NAME = "camera_screen"
                const val AREA = "camera"

                const val ID_GALLERY = "camera_button_01"
                const val ID_PHOTO = "camera_button_02"
                const val ID_SCANNER = "camera_button_03"
                const val ID_INTERSTITIAL_AD = "camera_ad_01"

                const val ITEM_NAME_SCANNER = "button_scan_documents"
                const val ITEM_NAME_PHOTO = "button_take_photo"
                const val ITEM_NAME_GALLERY = "button_gallery"
                const val ITEM_NAME_INTERSTITIAL_AD = "interstitial_ad"
            }
        }

        class Preview {
            companion object {
                const val SCREEN_NAME = "preview_screen"
                const val AREA = "preview"

                const val ID_BACK = "preview_button_01"
                const val ID_CROP = "preview_button_02"
                const val ID_ANALYZE = "preview_button_03"

                const val ITEM_NAME_BACK = "button_back"
                const val ITEM_NAME_CROP = "button_crop"
                const val ITEM_NAME_ANALYZE = "button_analyze"
            }
        }

        class Analyzer {
            companion object {
                const val SCREEN_NAME = "analyzer_screen"
                const val AREA = "analyzer"

                const val ID_BACK = "analyzer_button_01"
                const val ID_COPY = "analyzer_button_02"
                const val ID_SHARE = "analyzer_button_03"
                const val ID_REVIEW = "analyzer_button_04"
                const val ID_SAVE_CONTENT = "analyzer_button_05"
                const val ID_SAVE_PDF = "analyzer_button_05_01"
                const val ID_SAVE_TXT = "analyzer_button_05_02"
                const val ID_OPEN_SAVED_CONTENT = "analyzer_button_06"
                const val ID_BANNER_AD = "analyzer_ad_01"
                const val ID_EDIT_TEXT = "analyzer_edit_content"

                const val ITEM_NAME_BACK = "button_back"
                const val ITEM_NAME_COPY = "button_copy_content"
                const val ITEM_NAME_SHARE = "button_share_content"
                const val ITEM_NAME_REVIEW = "button_review_image"
                const val ITEM_NAME_SAVE_CONTENT = "button_save_content"
                const val ITEM_NAME_SAVE_PDF = "button_save_pdf"
                const val ITEM_NAME_SAVE_TXT = "button_save_txt"
                const val ITEM_NAME_OPEN_SAVED_CONTENT = "snackbar_open_saved_content"
                const val ITEM_NAME_BANNER_AD = "banner_ad"
                const val ITEM_NAME_EDIT_TEXT = "edit_analyzer"
            }
        }

        class Error{
            companion object{
                const val SCREEN_NAME = "error_screen"
                const val AREA = "error"

                const val ID_BACK = "error_button_01"
                const val ID_TRY_AGAIN = "error_button_02"


                const val ITEM_NAME_BACK = "button_back"
                const val ITEM_NAME_TRY_AGAIN = "button_try_again"
            }
        }
    }
}