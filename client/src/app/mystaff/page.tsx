"use client";
import { useState } from "react";
import Bigsquare from "../components/Bigsquare";
import ListBox from "../components/ListBox";
import MyStaffModal from "../components/MyStaffModal";
import Navi from "../Navi";
import { format } from "date-fns";
import CheckBox from "@material-ui/icons/CheckBox";

interface MyStaffData {
  staffnumber: number;
  memberName: string;
  department: string;

  rank: string;
  note: string;
  startTime: number;
  finishTime: number;
  management: string;
  salary: number;
}

export default function Mystaff() {
  const today = format(new Date(), "yyyy.MM.dd");

  const data: MyStaffData[] = [
    {
      staffnumber: 2023001,
      memberName: "홍길동",
      department: "회계팀",
      rank: "사원",
      note: "지각",
      startTime: 900,
      finishTime: 1800,
      management: "standard",
      salary: 4000000,
    },
    {
      staffnumber: 2023002,
      memberName: "김철수",
      department: "마케팅팀",
      rank: "대리",
      note: "결근",
      startTime: 1000,
      finishTime: 1900,
      management: "flextime",
      salary: 4500000,
    },
    //...
  ];

  const [showModal, setShowModal] = useState(false);
  return (
    <>
      <Navi />
      <div className="flex flex-col ">
        <div className="flex justify-end pb-3">
          <h1 className="pr-10">우리 회사의 근무시간 : 09:00~18:00</h1>
          <i className="material-icons"></i>
          <CheckBox />
          <h1 className="mr-8">오늘의 근무상황</h1>
        </div>
        <div className="h-screen w-screen flex justify-center">
          <Bigsquare>
            <div className="bg-white p-3 m-5 flex justify-between">
              <div>나의 직원들</div>
              <div>{today}</div>
            </div>

            <ListBox>
              <div>사번</div>
              <div className="lg:pl-10">이름</div>
              <div className="lg:pl-2">부서</div>
              <div className="lg:pl-1">직급</div>
              <div>근무상황</div>
              <div>기본근무시간</div>
              <div className="sm:mr-20 lg:mr-40 lg:pr-20">관리권한</div>
            </ListBox>
            {data ? (
              <>
                {data.map((item, index) => (
                  <ListBox key={index}>
                    <div>{item.staffnumber}</div>
                    <div>{item.memberName}</div>
                    <div>{item.department}</div>
                    <div>{item.rank}</div>
                    <div>
                      {item.finishTime} {item.note}
                    </div>
                    <div>
                      {item.startTime}~{item.finishTime}
                    </div>
                    <div className="pr-5">{item.management}</div>
                    <button
                      className="text-sm font-bold hover:bg-gray-300"
                      onClick={() => setShowModal(true)}
                    >
                      수정
                    </button>

                    {showModal && (
                      <MyStaffModal
                        onClose={() => setShowModal(false)}
                      ></MyStaffModal>
                    )}
                  </ListBox>
                ))}
              </>
            ) : null}
          </Bigsquare>
        </div>
      </div>
    </>
  );
}
