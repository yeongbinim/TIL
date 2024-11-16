#!/bin/bash

# 폴더 이름 생성하여 반환하는 함수
generate_folder_name() {
    local title="$1"
    local date_prefix=$(date +%y%m%d)
    echo "${date_prefix}_${title}"
}

# README 파일 생성 함수
create_readme() {
    local folder_name="$1"
    local title="$2"
    echo "# ${title}" > "${folder_name}/README.md"
}

# 현재 디렉터리의 README.md에 새 행 추가
update_main_readme() {
    local folder_name="$1"
    local title="$2"
    local entry="| [${title}](./${folder_name}) |  |"
    echo "$entry" >> README.md 
}

main() {
    echo "제목을 입력하세요: "
    read title

    if [[ -z "$title" ]]; then
        echo "제목이 입력되지 않았습니다."
        exit 1
    fi

    folder_name=$(generate_folder_name "$title")
    mkdir -p "$folder_name"
    create_readme "$folder_name" "$title"

    if [[ -f "README.md" ]]; then
        update_main_readme "$folder_name" "$title"
        echo "메인 README.md 파일이 업데이트 되었습니다."
    else
        echo "메인 README.md 파일이 존재하지 않습니다."
    fi

    echo "폴더 '$folder_name'가 생성되었으며, README.md 파일이 작성되었습니다."
}

main  # 스크립트 실행